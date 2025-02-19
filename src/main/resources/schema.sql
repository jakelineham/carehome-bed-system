drop table if exists aftercare;
create table if not exists aftercare(
    id int auto_increment primary key,
    homeName varchar(150) not null,
    address varchar(50) not null,
    email varchar(50) not null,
    phoneNo varchar(50) not null,
    area varchar(50) not null,
    has12HourCare boolean not null,
    speaksWelsh boolean not null,
    assistedLiving boolean not null,
    totalNumOfBeds int not null,
    generalCareTotal int,
    generalCareAvailable int,
    mentalHealthTotal int,
    mentalHealthAvailable int,
    dementiaTotal int,
    dementiaAvailable int,
    rehabTotal int,
    rehabAvailable int,
    palliativeTotal int,
    palliativeAvailable int,
    accepted bool not null
) engine=InnoDB;

drop table if exists areaMonthlyOverview;
create table if not exists areaMonthlyOverview(
    month int not null,
    year int not null,
    area varchar(150) not null,
    bedFillPercentage float not null
) engine=InnoDB;

drop table if exists userAccounts;
create table if not exists userAccounts(
    id int auto_increment primary key,
    email varchar(100) not null,
    name varchar(150) not null,
    password varchar(200) not null,
    assignedCarehomeId int not null,
    area varchar(150) not null,
    accepted bool not null
) engine=InnoDB;

drop table if exists roles;
create table if not exists roles(
    roleID int auto_increment primary key,
    roleName varchar(100) not null
) engine=InnoDB;

drop table if exists userAccounts_roles;
create table if not exists userAccounts_roles(
    email varchar(100) not null,
    roleID int not null
) engine=InnoDB;

create view if not exists userAuthorities as
select u.email as username, CONCAT("ROLE_", r.roleName) as authority from userAccounts u
    inner join userAccounts_roles ur on u.email = ur.email
    inner join roles r on ur.roleID = r.roleID;

CREATE TABLE IF NOT EXISTS contact_messages (
                                                id INT AUTO_INCREMENT PRIMARY KEY,
                                                name VARCHAR(30) NOT NULL,
    email VARCHAR(50) NOT NULL,
    user_type ENUM('Social Services', 'Society Manager', 'Carehome Manager', 'Stakeholder') NOT NULL,
    message TEXT NOT NULL,
    CHECK (CHAR_LENGTH(name) >= 2),
    CHECK (CHAR_LENGTH(message) BETWEEN 10 AND 1200)
    );
