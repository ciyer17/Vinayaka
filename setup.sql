-- Create a new user with the specified password
CREATE USER 'vinayaka'@'localhost' IDENTIFIED BY 'vinayaka_108';
CREATE DATABASE appdata;

-- Grant all privileges on the "appdata" database to the new user
GRANT ALL PRIVILEGES ON appdata.* TO 'vinayaka'@'localhost';
FLUSH PRIVILEGES;
