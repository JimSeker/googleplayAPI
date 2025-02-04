
You will need a MariaDB and node system to serve these from.  You won't run this on the phone.


to setup the database:

mysql -u root -p  //or administrative access to db

//create the database and a user  Change 'password' to a real password.
create database FcmDemo;
grant all privileges on FcmDemo.* to fcm@localhost identified by 'password' with grant option;

CREATE TABLE `devices` (
  `id` int(11) PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `token` text NOT NULL
);


Then change the endpoints class to your system

npm install to restore the modules.
