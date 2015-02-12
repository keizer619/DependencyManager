-- MySQL dump 10.13  Distrib 5.5.40, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: DependencyManager
-- ------------------------------------------------------
-- Server version	5.5.40-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `DependencyTable`
--

DROP TABLE IF EXISTS `DependencyTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DependencyTable` (
  `GroupId` varchar(45) NOT NULL,
  `ArtifactId` varchar(45) NOT NULL,
  `Version` varchar(45) NOT NULL,
  `LatestVersion` varchar(45) DEFAULT NULL,
  `SourceRepoId` int(11) DEFAULT NULL,
  PRIMARY KEY (`GroupId`,`ArtifactId`,`Version`),
  KEY `SourceRepoId_idx` (`SourceRepoId`),
  CONSTRAINT `SourceRepoId` FOREIGN KEY (`SourceRepoId`) REFERENCES `RepositoryTable` (`RepoID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DependencyTable`
--

LOCK TABLES `DependencyTable` WRITE;
/*!40000 ALTER TABLE `DependencyTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `DependencyTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RepositoryDependencyTable`
--

DROP TABLE IF EXISTS `RepositoryDependencyTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RepositoryDependencyTable` (
  `GroupId` varchar(45) NOT NULL,
  `ArtifactID` varchar(45) NOT NULL,
  `Version` varchar(45) NOT NULL,
  `SourceRepoId` int(11) NOT NULL,
  PRIMARY KEY (`GroupId`,`ArtifactID`,`Version`,`SourceRepoId`),
  KEY `fk_RepositoryDependencyTable2_2_idx` (`SourceRepoId`),
  CONSTRAINT `fk_RepositoryDependencyTable2_1` FOREIGN KEY (`GroupId`, `ArtifactID`, `Version`) REFERENCES `DependencyTable` (`GroupId`, `ArtifactId`, `Version`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_RepositoryDependencyTable2_2` FOREIGN KEY (`SourceRepoId`) REFERENCES `RepositoryTable` (`RepoID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_RepositoryDependencyTable_1` FOREIGN KEY (`SourceRepoId`) REFERENCES `RepositoryTable` (`RepoID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RepositoryDependencyTable`
--

LOCK TABLES `RepositoryDependencyTable` WRITE;
/*!40000 ALTER TABLE `RepositoryDependencyTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `RepositoryDependencyTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RepositoryTable`
--

DROP TABLE IF EXISTS `RepositoryTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RepositoryTable` (
  `RepoID` int(11) NOT NULL AUTO_INCREMENT,
  `RepoName` varchar(45) DEFAULT NULL,
  `Remarks` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`RepoID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RepositoryTable`
--

LOCK TABLES `RepositoryTable` WRITE;
/*!40000 ALTER TABLE `RepositoryTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `RepositoryTable` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-02-11  9:45:19
