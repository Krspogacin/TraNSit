const express = require('express');
const app = express();
const port = process.env.port || 3000;
const morgan = require('morgan');
const controller = require('./controller');
const sequelize = require('./sequelize');

const Line = require("./model/Line.js");

app.use(morgan('dev'));

app.use('/api', controller);

app.use((req, res, next) => {
    res.status(404).json({
        error: {
            message: 'Not found!'
        }
    });
});

sequelize.authenticate()
    .then(() => {
        console.log('DB connection has been established successfully.');
        sequelize.sync({ force: true }).then(() => {
            console.log('Drop and re-sync DB successful!');
            app.listen(port, () => {
                console.log(`App listening on port ${port}`);
                console.log('Press Ctrl+C to quit.');


                const line1 = Line.build({ name: '1', title: 'KLISA - LIMAN 1', type: 'CITY' });
                console.log(line1 instanceof Line); // true
                console.log(line1.title); // 'KLISA - LIMAN 1'

                line1.save().then(
                    () => {
                        console.log('line1 was saved to the database!');
                    });

            });
        });
    })
    .catch(error => {
        console.error('Unable to connect to the DB. Error: ', error);
    });