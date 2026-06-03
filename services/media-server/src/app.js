const express = require('express');
const multer = require('multer');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(cors());
app.use(express.json());

// File upload storage
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/');
  },
  filename: (req, file, cb) => {
    cb(null, Date.now() + '-' + file.originalname);
  }
});

const upload = multer({ storage });

// Routes
app.post('/api/v1/media/upload', upload.single('file'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No file uploaded' });
  }
  res.json({
    id: req.file.filename,
    url: `/api/v1/media/${req.file.filename}`,
    filename: req.file.originalname
  });
});

app.get('/api/v1/media/:fileId', (req, res) => {
  res.download(path.join('uploads', req.params.fileId));
});

app.delete('/api/v1/media/:fileId', (req, res) => {
  // TODO: Implement file deletion
  res.json({ success: true });
});

app.listen(PORT, () => {
  console.log(`Media Server running on port ${PORT}`);
});
