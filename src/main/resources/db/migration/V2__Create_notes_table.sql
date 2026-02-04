CREATE TABLE notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    content TEXT,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);