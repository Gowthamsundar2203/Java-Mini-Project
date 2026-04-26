CREATE DATABASE parking_system;
USE parking_system;

CREATE TABLE Slots (
  id INT PRIMARY KEY AUTO_INCREMENT,
  slot_number VARCHAR(10),
  status VARCHAR(10)
);

CREATE TABLE Vehicles (
  id INT PRIMARY KEY AUTO_INCREMENT,
  vehicle_number VARCHAR(20),
  owner_name VARCHAR(50)
);

CREATE TABLE ParkingRecords (
  id INT PRIMARY KEY AUTO_INCREMENT,
  vehicle_id INT,
  slot_id INT,
  entry_time DATETIME,
  exit_time DATETIME,
  fee DOUBLE
);

INSERT INTO Slots VALUES
(1,'A1','FREE'),
(2,'A2','FREE'),
(3,'A3','FREE');

INSERT INTO Slots VALUES

(4,'B1','FREE'),(5,'B2','FREE'),(6,'B3','FREE');