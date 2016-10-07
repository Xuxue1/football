create database if not exists football;

use football;

create table game(
    source int,
    id int,
    leaglue varchar(15),
    gametime datetime,
    status varchar(5),
    morder varchar(10),
    mteam varchar(15),
    score varchar(15),
    oteam varchar(15),
    oorder varchar(10),
    jingcai int,
    danchang int,
    zucai int,
    turn varchar(15),
    primary key(source,id)
);


create table aoke_pankou(
    name varchar(15),
    value int,
    primary key(name)
);

create table odds(
    source int,
    id int,
    company int,
    type int,
    updateTime datetime,
    d1 double,
    d2 DOUBLE ,
    d3 DOUBLE,
    primary key(source,id,company,type)
);

create table map(
    id int auto_increment,
    type int,
    name varchar(20),
    primary key(id,type)
);


