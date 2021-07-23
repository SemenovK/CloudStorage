CREATE DATABASE  IF NOT EXISTS `cloudstorage` /*!40100 DEFAULT CHARACTER SET cp1251 COLLATE cp1251_general_cs */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `cloudstorage`;
-- MySQL dump 10.13  Distrib 8.0.25, for Win64 (x86_64)
--
-- Host: localhost    Database: cloudstorage
-- ------------------------------------------------------
-- Server version	8.0.25

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `shared_folder_to`
--

DROP TABLE IF EXISTS `shared_folder_to`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shared_folder_to` (
  `id` int NOT NULL AUTO_INCREMENT,
  `folderid` int NOT NULL,
  `userid` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=cp1251 COLLATE=cp1251_general_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shared_folder_to`
--

LOCK TABLES `shared_folder_to` WRITE;
/*!40000 ALTER TABLE `shared_folder_to` DISABLE KEYS */;
INSERT INTO `shared_folder_to` VALUES (13,15,2),(14,16,2),(16,18,2),(17,19,2);
/*!40000 ALTER TABLE `shared_folder_to` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) COLLATE cp1251_general_cs NOT NULL,
  `login` varchar(45) COLLATE cp1251_general_cs NOT NULL,
  `password` varchar(45) COLLATE cp1251_general_cs NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `login_UNIQUE` (`login`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=cp1251 COLLATE=cp1251_general_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'First User','User1','pass1'),(2,'Second User','User2','pass2'),(3,'Third User','User3','pass3'),(4,'Fourth User','User4','pass4');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_shared_folder`
--

DROP TABLE IF EXISTS `users_shared_folder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_shared_folder` (
  `id` int NOT NULL AUTO_INCREMENT,
  `userid` int NOT NULL,
  `folder` varchar(500) COLLATE cp1251_general_cs NOT NULL,
  `folderpath` varchar(500) COLLATE cp1251_general_cs DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `usersFolders` (`userid`,`folder`) /*!80000 INVISIBLE */
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=cp1251 COLLATE=cp1251_general_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_shared_folder`
--

LOCK TABLES `users_shared_folder` WRITE;
/*!40000 ALTER TABLE `users_shared_folder` DISABLE KEYS */;
INSERT INTO `users_shared_folder` VALUES (15,1,'src','C:\\TMP\\1\\DoodleChat\\DoodleChatClient'),(16,3,'Folder2','C:\\TMP\\3'),(18,1,'Test','C:\\TMP\\1'),(19,1,'Folder2','C:\\TMP\\1');
/*!40000 ALTER TABLE `users_shared_folder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'cloudstorage'
--
/*!50003 DROP PROCEDURE IF EXISTS `add_shared_folder` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `add_shared_folder`(in _UserId integer,
                                      in _folderName varchar(500),
                                      in _folderPath varchar(500),
                                      in _toUserId integer,
									  out _retval VARCHAR(5), 
							          out _error_message varchar(255))
BEGIN
 declare exit handler for sqlexception
    begin
		get diagnostics condition 1
		_retval = RETURNED_SQLSTATE,
        _error_message = MESSAGE_TEXT;
    end;
    
    set _retval = '00000';
    set _error_message = 'Successful complete';
    if exists(select * from users where id = _UserId) then


if not exists(select * from users_shared_folder 
                      where userid = _UserId
                        and folder = _folderName) then
       
       insert users_shared_folder(userid, folder, folderpath)
        values(_UserID, _FolderName, _folderPath);
end if;

SELECT 
    id
INTO @folderId 
FROM users_shared_folder
WHERE userid = _UserID
  AND folder = _folderName;

if not exists(select * from shared_folder_to where folderid = @folderId 
                                            and userid = _toUserId) then
insert into shared_folder_to(folderid, userid) 
values (@folderId, _toUserId);  
end if;

	else 
        set _retval = -1;
        set _error_message = 'Wrong user number.' ;
    end if;
    

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `remove_shared_folder` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `remove_shared_folder`(in _UserId integer,
                                      in _folderName varchar(500),
                                      in _toUserId integer,
									  out _retval VARCHAR(5), 
							          out _error_message varchar(255))
BEGIN

 declare exit handler for sqlexception
    begin
		get diagnostics condition 1
		_retval = RETURNED_SQLSTATE,
        _error_message = MESSAGE_TEXT;
    end;
    SET SQL_SAFE_UPDATES = 0;
    set _retval = '00000';
    set _error_message = 'Successful complete';
    if exists(select * from users where id = _UserId) then

set @folderId = 0;

select id into @folderId from users_shared_folder 
                      where userid = _UserId
                        and folder = _folderName;
                        
if (@folderId != 0) then
	delete from shared_folder_to where folderid = @folderId and userid = _toUserId;
     
	select count(*) into @count from  shared_folder_to where folderid = @folderId;
    if(@count = 0) then
    delete from users_shared_folder 
		  where userid = _UserId
		    and folder = _folderName;
    end if;
    
end if;


	else 
        set _retval = -1;
        set _error_message = 'Wrong user number.' ;
    end if;
    

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-07-23 15:10:44
