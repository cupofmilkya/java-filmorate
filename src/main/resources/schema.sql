-- ==========================
-- Возрастные рейтинги (MPA)
-- ==========================
CREATE TABLE IF NOT EXISTS mpa_rating (
    mpa_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- ==========================
-- Таблица пользователей
-- ==========================
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL,
    name VARCHAR(100),
    birthday DATE NOT NULL
);

-- ==========================
-- Таблица фильмов
-- ==========================
CREATE TABLE IF NOT EXISTS films (
    film_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    mpa_id INT NOT NULL,
    CONSTRAINT fk_mpa_rating FOREIGN KEY (mpa_id)
        REFERENCES mpa_rating(mpa_id)
);

-- ==========================
-- Жанры
-- ==========================
CREATE TABLE IF NOT EXISTS genre (
    genre_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- ==========================
-- Связь фильмов и жанров Many-to-Many
-- ==========================
CREATE TABLE IF NOT EXISTS genre_film (
    film_id BIGINT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id),
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
);

-- ==========================
-- Лайки пользователей
-- ==========================
CREATE TABLE IF NOT EXISTS likes (
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ==========================
-- Дружба с подтверждением
-- ==========================
CREATE TABLE IF NOT EXISTS user_friendships (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    confirmed BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CHECK (user_id <> friend_id)
);

