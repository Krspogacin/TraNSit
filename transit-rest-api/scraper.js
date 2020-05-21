const axios = require('axios')
const osmosis = require('osmosis')
const _ = require('underscore')

async function fetchLines() {
    const url = 'http://gspns.rs/mreza'
    let p = new Promise((resolve, reject) => {
        let lines = []
        osmosis.get(url)
            .find('#button-linije ul li')
            .set({
                id: 'a @id',
                title: 'a @title',
                name: 'a',
                type: 'a @class'
            })
            .data(line => {
                if (line.type.indexOf(' medjumesni ') !== -1) {
                    line.type = 3
                } else if (line.type.indexOf(' prigrad ') !== -1) {
                    line.type = 2
                } else {
                    line.type = 1
                }
                lines.push(line)
            })
            .done(() => {
                resolve(lines)
            })
            .error(reject)
    })
    return p
}

function fetchLine(lineNumber) {
    return axios.get(`http://www.gspns.co.rs/mreza-get-linija-tacke?linija=${lineNumber}`)
        .then(response => {
            return response.data
                .filter(coordStr => { return coordStr.split(',').length === 2 })
                .map(coordStr => { return { lat: coordStr.split(',')[0].trim(), lon: coordStr.split(',')[1].trim() } })
        })
}

async function fetchLinesCoordinates() {
    const linesData = await fetchLines()
    const linesPromises = linesData.map(line => {
        return fetchLine(line.id).then(coords => line.coordinates = coords)
    })
    await Promise.all(linesPromises)
    return linesData
}

async function fetchStops() {
    const linesData = await fetchLines()
    let allStops = []
    const stopsPromises = linesData.map(line => {
        return axios.get(`http://www.gspns.co.rs/mreza-get-stajalista-tacke?linija=${line.id}`)
            .then(response => {
                let stops = response.data
                    // [1A],[1ZA],[3B],[8A],[19A]|19.8416504854|45.255203707|USPENSKA - \u0160AFARIKOVA|http:\/\/www.mapanovisad.rs\/stajalista_gsp\/img002617.jpg|I
                    .map(stopStr => stopStr.split('|'))
                    .map(stopArray => {
                        return {
                            lines: stopArray[0].trim().split(',').map(line => line.replace(/\[(.*)\]/, '$1')),
                            lat: stopArray[2].trim(),
                            lon: stopArray[1].trim(),
                            name: stopArray[3],
                            photo: stopArray[4],
                            zone: stopArray[5]
                        }
                    })
                allStops.push(stops)
            })
    })
    await Promise.all(stopsPromises)
    allStops = _.unique(allStops, false, JSON.stringify)
    return allStops
}

async function fetchTimeTables() {
    let lines = await fetchLines()
    let linesTimeTables = {}
    let timeTableMeta = await fetchTimeTableMeta()
    console.log('meta time table: ')
    console.log(timeTableMeta)
    for (let dayType of timeTableMeta.dayTypes) {
        linesTimeTables[dayType] = []
        let linesPromises = timeTableMeta.lineTypes.map(async lineType => {
            return fetchTimeTableLines(lineType, timeTableMeta.datesFrom[0], dayType)
                .then(fetchedLines => {
                    linesTimeTables[dayType].push(...fetchedLines)
                })
        })
        await Promise.all(linesPromises)
    }
    for (let dayType of timeTableMeta.dayTypes) {
        linesPromises = linesTimeTables[dayType].map(line => {
            return fetchTimeTable(line.lineType, timeTableMeta.datesFrom[0], dayType, line.key).then(tt => {
                line.timeTable = tt
                fillTimeTableForLine(lines, line, dayType)
            }).catch(error => {
                console.log(`failed fetching time table for ${line.key}`)
                console.log(error)
            })
        })
        await Promise.all(linesPromises)
    }

    return lines
}

async function fetchTimeTableMeta() {
    const url = 'http://gspns.rs/red-voznje/gradski'
    let p = new Promise((resolve, reject) => {
        osmosis.get(url)
            .find('#rvform')
            .set({
                datesFrom: ['#vaziod option @value'],
                lineTypes: ['#rv option @value'],
                dayTypes: ['#dan option @value']
            })
            .data(data => {
                resolve(data)
            })
            .error(reject)
    })
    return p
}

async function fetchTimeTableLines(lineType, dateFrom, dayType) {
    const url = `http://gspns.rs/red-voznje/lista-linija?rv=${lineType}&vaziod=${dateFrom}&dan=${dayType}`
    let lines = []
    let p = new Promise((resolve, reject) => {
        osmosis.get(url)
            .find('#linija option')
            .set({
                key: '. @value',
                name: '.'
            })
            .data(data => {
                data.lineType = lineType
                lines.push(data)
            })
            .error(reject)
            .done(() => {
                resolve(lines)
            })
    })
    return p
}

const strRegex = /[^0-9]*/gi
const numRegex = /\d*/gi
const circularLines = [7, 11, 18];

async function fetchTimeTable(lineType, dateFrom, dayType, line) {
    const url = `http://gspns.rs/red-voznje/ispis-polazaka?rv=${lineType}&vaziod=${dateFrom}&dan=${dayType}&linija%5B%5D=${line}`
    // console.log(`fetching time tables for line ${line} from url ${url}`)
    let p = new Promise((resolve, reject) => {
        let timetable = [[], []]
        let currentHour = ''
        let dir = 0
        osmosis.get(url)
            .find('tr > td > b, tr > td > sup > font > span')
            // .find('tr > td, tr > td > sup > font > span')
            .set('d')
            .then((context, data) => {
                // minutes have a 'b' tag inside, hours don't
                if (context.find('b').length === 0) {
                    if ((currentHour > data.d && data.d !== '00') || currentHour === '00') {
                        dir = 1
                    }
                    currentHour = data.d
                } else {
                    timetable[dir].push({
                        formatted: `${currentHour}:${data.d}`,
                        hour: currentHour,
                        minute: data.d.replace(strRegex, ''),
                        label: data.d.replace(numRegex, '')
                    })
                }
                if (context.last) {
                    resolve(timetable)
                }
            })
            .error(() => {
                console.log('Error: ' + url)
                reject()
            })
    })
    return p
}


function fillTimeTableForLine(linesData, lineTimeTable, dayType) {
    for (let lineData of linesData) {
        if (!lineData.timeTable) {
            lineData.timeTable = {}
        }
        const name = lineData.name;
        const lineKey = name.substring(0, name.length - 1) + ' '
        const direction = name.substring(name.length - 1)
        let line = lineTimeTable.name.indexOf(lineKey) === 0
        const isCircularLine = circularLines.includes(parseInt(lineKey))

        if (!line && isCircularLine) {
            line = lineTimeTable.name.indexOf(name + ' ') === 0
        }
        if (line) {
            lineData.timeTable[getDayTypeLabel(dayType)] =
                lineTimeTable.timeTable[direction === 'A' || isCircularLine ? 0 : 1].map(departure => `${departure.hour}:${departure.minute}`)
        }
    }
}

function getDayTypeLabel(dayType) {
    switch (dayType) {
        case 'R':
            return 'workday'
        case 'S':
            return 'saturday'
        case 'N':
            return 'sunday'
        default:
            throw new Error('Unknown day type: ' + dayType)
    }
}

module.exports = { fetchLines, fetchLinesCoordinates, fetchStops, fetchTimeTables }
