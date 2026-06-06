import 'dotenv/config';
import app from './app.js';
import sequelize from './config/database.js';

const PORT = process.env.PORT || 3001;

sequelize.sync({ alter: process.env.NODE_ENV === 'development' })
  .then(() => {
    console.log('Database synchronized successfully.');
    
    app.listen(PORT, () => {
      console.log(`Media Server running on port ${PORT}`);
    });
  })
  .catch((error) => {
    console.error('Unable to connect to the database or sync models:', error);
    process.exit(1);
  });

process.on('SIGTERM', () => {
  console.log('SIGTERM signal received. Closing HTTP server gracefully...');
  sequelize.close().then(() => {
    console.log('Database connections closed.');
    process.exit(0);
  });
});