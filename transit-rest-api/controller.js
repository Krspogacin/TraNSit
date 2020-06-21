const express = require('express');
const router = express.Router();
const scraper = require('./scraper');

router.route('/lines')
    .get(getLines);

router.route('/zones')
    .get(getZones);

router.route('/lines-coordinates')
    .get(getLinesCoordinates);

router.route('/stops')
    .get(getStops);

router.route('/time-tables')
    .get(getTimeTables);

/////////////////////////////////////////////////////////

async function getLines(req, res) {
    const lines = await scraper.fetchLines();
    res.status(200)
        .send(lines);
}

async function getZones(req, res) {
    const zones = await scraper.fetchZones();
    res.status(200)
        .send(zones);
}

async function getLinesCoordinates(req, res) {
    const linesCoordinates = await scraper.fetchLinesCoordinates();
    res.status(200)
        .send(linesCoordinates);
}

async function getStops(req, res) {
    const stops = await scraper.fetchStops();
    res.status(200)
        .send(stops);
}

async function getTimeTables(req, res) {
    const timeTables = await scraper.fetchTimeTables();
    res.status(200)
        .send(timeTables);
}

module.exports = router;