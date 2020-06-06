const express = require('express');
const app = express();
const port = process.env.port || 3000;
const morgan = require('morgan');
const controller = require('./controller');

app.use(morgan('dev'));

app.use('/api', controller);

app.use((req, res, next) => {
    res.status(404).json({
        error: {
            message: 'Not found!'
        }
    });
});

app.listen(port, () => {
    console.log(`App listening on port ${port}`);
    console.log('Press Ctrl+C to quit.');
});