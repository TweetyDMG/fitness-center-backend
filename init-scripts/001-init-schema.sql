-- =============================================================================
-- Fitness Center Database Initialization Script
-- =============================================================================
-- This script runs on first MSSQL container startup to create the database
-- and apply the initial schema. Add this as a volume mount in docker-compose.
-- =============================================================================

-- Create the database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'FitnessCenter')
BEGIN
    CREATE DATABASE FitnessCenter;
END
GO

USE FitnessCenter;
GO

-- =============================================================================
-- Tables
-- =============================================================================

-- Client
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[client]') AND type in (N'U'))
BEGIN
    CREATE TABLE client (
        ID_Client BIGINT IDENTITY(1,1) PRIMARY KEY,
        Firstname NVARCHAR(100) NOT NULL,
        Lastname NVARCHAR(100) NOT NULL,
        Patronymic NVARCHAR(100),
        Phone NVARCHAR(20) NOT NULL,
        Gender NVARCHAR(10) NOT NULL,
        Email NVARCHAR(255) NOT NULL,
        Passport NVARCHAR(20)
    );
END
GO

-- Trainer
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[trainer]') AND type in (N'U'))
BEGIN
    CREATE TABLE trainer (
        ID_Trainer BIGINT IDENTITY(1,1) PRIMARY KEY,
        Firstname NVARCHAR(100) NOT NULL,
        Lastname NVARCHAR(100) NOT NULL,
        Patronymic NVARCHAR(100),
        Specialization NVARCHAR(255)
    );
END
GO

-- Users
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND type in (N'U'))
BEGIN
    CREATE TABLE users (
        ID_User BIGINT IDENTITY(1,1) PRIMARY KEY,
        Username NVARCHAR(50) NOT NULL UNIQUE,
        password_hash NVARCHAR(255) NOT NULL,
        Role NVARCHAR(20) NOT NULL CHECK (Role IN ('CLIENT', 'TRAINER', 'MANAGER', 'ADMIN')),
        Client_ID_Client BIGINT,
        Trainer_ID_Trainer BIGINT,
        CONSTRAINT FK_User_Client FOREIGN KEY (Client_ID_Client) REFERENCES client(ID_Client),
        CONSTRAINT FK_User_Trainer FOREIGN KEY (Trainer_ID_Trainer) REFERENCES trainer(ID_Trainer)
    );
END
GO

-- Discount
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[discount]') AND type in (N'U'))
BEGIN
    CREATE TABLE discount (
        ID_Discount BIGINT IDENTITY(1,1) PRIMARY KEY,
        Name NVARCHAR(255) NOT NULL,
        Percent DECIMAL(5,2) NOT NULL,
        StartDate DATE,
        EndDate DATE
    );
END
GO

-- FitnessCenter
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[fitness_center]') AND type in (N'U'))
BEGIN
    CREATE TABLE fitness_center (
        ID_Fitness_Center BIGINT IDENTITY(1,1) PRIMARY KEY,
        Name NVARCHAR(255) NOT NULL,
        Address NVARCHAR(500) NOT NULL,
        Phone NVARCHAR(20)
    );
END
GO

-- Subscription
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[subscription]') AND type in (N'U'))
BEGIN
    CREATE TABLE subscription (
        ID_Subscription BIGINT IDENTITY(1,1) PRIMARY KEY,
        Name NVARCHAR(255) NOT NULL,
        Price DECIMAL(10,2) NOT NULL,
        DurationDays INT NOT NULL,
        VisitCount INT NOT NULL,
        Description NVARCHAR(500)
    );
END
GO

-- Sale
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sale]') AND type in (N'U'))
BEGIN
    CREATE TABLE sale (
        ID_Sale BIGINT IDENTITY(1,1) PRIMARY KEY,
        BankCardNum NVARCHAR(19),
        StartDate DATE NOT NULL,
        EndDate DATE NOT NULL,
        Client_ID_Client BIGINT NOT NULL,
        Subscription_ID_Subscription BIGINT NOT NULL,
        Discount_ID_Discount BIGINT,
        FitnessCenter_ID_Fitness_Center BIGINT NOT NULL,
        CONSTRAINT FK_Sale_Client FOREIGN KEY (Client_ID_Client) REFERENCES client(ID_Client),
        CONSTRAINT FK_Sale_Subscription FOREIGN KEY (Subscription_ID_Subscription) REFERENCES subscription(ID_Subscription),
        CONSTRAINT FK_Sale_Discount FOREIGN KEY (Discount_ID_Discount) REFERENCES discount(ID_Discount),
        CONSTRAINT FK_Sale_FitnessCenter FOREIGN KEY (FitnessCenter_ID_Fitness_Center) REFERENCES fitness_center(ID_Fitness_Center)
    );
END
GO

-- Schedule
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[schedule]') AND type in (N'U'))
BEGIN
    CREATE TABLE schedule (
        ID_Schedule BIGINT IDENTITY(1,1) PRIMARY KEY,
        DayOfWeek INT NOT NULL CHECK (DayOfWeek BETWEEN 1 AND 7),
        StartTime TIME NOT NULL,
        EndTime TIME NOT NULL,
        Trainer_ID_Trainer BIGINT NOT NULL,
        Service_ID_Service BIGINT NOT NULL,
        FitnessCenter_ID_Fitness_Center BIGINT NOT NULL,
        MaxClients INT NOT NULL DEFAULT 1,
        CONSTRAINT FK_Schedule_Trainer FOREIGN KEY (Trainer_ID_Trainer) REFERENCES trainer(ID_Trainer),
        CONSTRAINT FK_Schedule_FitnessCenter FOREIGN KEY (FitnessCenter_ID_Fitness_Center) REFERENCES fitness_center(ID_Fitness_Center)
    );
END
GO

-- FitnessService
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[fitness_service]') AND type in (N'U'))
BEGIN
    CREATE TABLE fitness_service (
        ID_Service BIGINT IDENTITY(1,1) PRIMARY KEY,
        Name NVARCHAR(255) NOT NULL,
        Description NVARCHAR(500),
        Price DECIMAL(10,2) NOT NULL
    );
END
GO

-- Add FK from Schedule to FitnessService after both tables exist
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Schedule_Service')
    ALTER TABLE schedule DROP CONSTRAINT FK_Schedule_Service;
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[FK_Schedule_Service]') AND type in (N'F'))
BEGIN
    ALTER TABLE schedule ADD CONSTRAINT FK_Schedule_Service
        FOREIGN KEY (Service_ID_Service) REFERENCES fitness_service(ID_Service);
END
GO

-- RegistrationOfVisit
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[registration_of_visit]') AND type in (N'U'))
BEGIN
    CREATE TABLE registration_of_visit (
        ID_Registration BIGINT IDENTITY(1,1) PRIMARY KEY,
        VisitDate DATE NOT NULL,
        Status NVARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
        Client_ID_Client BIGINT NOT NULL,
        Schedule_ID_Schedule BIGINT NOT NULL,
        CONSTRAINT FK_Visit_Client FOREIGN KEY (Client_ID_Client) REFERENCES client(ID_Client),
        CONSTRAINT FK_Visit_Schedule FOREIGN KEY (Schedule_ID_Schedule) REFERENCES schedule(ID_Schedule)
    );
END
GO

-- Recommendation
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[recommendation]') AND type in (N'U'))
BEGIN
    CREATE TABLE recommendation (
        ID_Recommendation BIGINT IDENTITY(1,1) PRIMARY KEY,
        Text NVARCHAR(MAX) NOT NULL,
        Date DATE NOT NULL,
        Client_ID_Client BIGINT NOT NULL,
        Trainer_ID_Trainer BIGINT NOT NULL,
        CONSTRAINT FK_Recommendation_Client FOREIGN KEY (Client_ID_Client) REFERENCES client(ID_Client),
        CONSTRAINT FK_Recommendation_Trainer FOREIGN KEY (Trainer_ID_Trainer) REFERENCES trainer(ID_Trainer)
    );
END
GO

-- DiseaseType
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[disease_type]') AND type in (N'U'))
BEGIN
    CREATE TABLE disease_type (
        ID_Disease_Type BIGINT IDENTITY(1,1) PRIMARY KEY,
        Name NVARCHAR(255) NOT NULL,
        Description NVARCHAR(500)
    );
END
GO

-- Disease
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[disease]') AND type in (N'U'))
BEGIN
    CREATE TABLE disease (
        ID_Disease BIGINT IDENTITY(1,1) PRIMARY KEY,
        Name NVARCHAR(255) NOT NULL,
        Client_ID_Client BIGINT NOT NULL,
        DiseaseType_ID_Disease_Type BIGINT NOT NULL,
        CONSTRAINT FK_Disease_Client FOREIGN KEY (Client_ID_Client) REFERENCES client(ID_Client),
        CONSTRAINT FK_Disease_DiseaseType FOREIGN KEY (DiseaseType_ID_Disease_Type) REFERENCES disease_type(ID_Disease_Type)
    );
END
GO

-- PreferenceType
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[preference_type]') AND type in (N'U'))
BEGIN
    CREATE TABLE preference_type (
        ID_Preference_Type BIGINT IDENTITY(1,1) PRIMARY KEY,
        Name NVARCHAR(255) NOT NULL
    );
END
GO

-- Preference
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[preference]') AND type in (N'U'))
BEGIN
    CREATE TABLE preference (
        ID_Preference BIGINT IDENTITY(1,1) PRIMARY KEY,
        Client_ID_Client BIGINT NOT NULL,
        PreferenceType_ID_Preference_Type BIGINT NOT NULL,
        CONSTRAINT FK_Preference_Client FOREIGN KEY (Client_ID_Client) REFERENCES client(ID_Client),
        CONSTRAINT FK_Preference_PreferenceType FOREIGN KEY (PreferenceType_ID_Preference_Type) REFERENCES preference_type(ID_Preference_Type)
    );
END
GO

-- PriceHistory
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[price_history]') AND type in (N'U'))
BEGIN
    CREATE TABLE price_history (
        ID_Price_History BIGINT IDENTITY(1,1) PRIMARY KEY,
        Price DECIMAL(10,2) NOT NULL,
        ChangeDate DATE NOT NULL,
        Service_ID_Service BIGINT NOT NULL,
        CONSTRAINT FK_PriceHistory_Service FOREIGN KEY (Service_ID_Service) REFERENCES fitness_service(ID_Service)
    );
END
GO

-- =============================================================================
-- Indexes
-- =============================================================================
CREATE NONCLUSTERED INDEX IX_Users_Username ON users(Username);
CREATE NONCLUSTERED INDEX IX_Sale_Client ON sale(Client_ID_Client);
CREATE NONCLUSTERED INDEX IX_Schedule_Trainer ON schedule(Trainer_ID_Trainer);
CREATE NONCLUSTERED INDEX IX_Visit_Client ON registration_of_visit(Client_ID_Client);
CREATE NONCLUSTERED INDEX IX_Visit_Schedule ON registration_of_visit(Schedule_ID_Schedule);
GO

PRINT 'FitnessCenter database schema initialized successfully.';
GO