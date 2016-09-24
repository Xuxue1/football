create database if not exists football;

use football;

create table game(
    source int,
    id int,
    game int,
    leaglue int,
    gametime datetime,
    status int,
    morder varchar(10),
    mteam int,
    score int,
    oteam int,
    oorder varchar(10),
    jingcai int,
    danchang int,
    zucai int,
    turn int,
    primary key(source,id)
);

create table odds(
    source int,
    id int,
    company int,
    type int,
    updateTime datetime,
    d1 double,
    d2 double,
    d3 double,
    primary key(source,id,company,type)
);

create table map(
    id int auto_increment,
    type int,
    name varchar(20),
    primary key(id,type)
);
