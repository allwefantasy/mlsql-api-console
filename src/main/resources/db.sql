# ************************************************************
# Sequel Pro SQL dump
# Version 4096
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: 127.0.0.1 (MySQL 5.7.20)
# Database: mlsql_console
# Generation Time: 2019-05-27 06:00:12 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table access_token
# ------------------------------------------------------------

DROP TABLE IF EXISTS `access_token`;

CREATE TABLE `access_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `mlsql_user_id` int(11) DEFAULT NULL,
  `create_at` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `mlsql_user_id` (`mlsql_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table aliyun_cluster_process
# ------------------------------------------------------------

DROP TABLE IF EXISTS `aliyun_cluster_process`;

CREATE TABLE `aliyun_cluster_process` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mlsql_user_id` int(11) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `start_time` text,
  `end_time` text,
  `reason` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table mlsql_backend_proxy
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mlsql_backend_proxy`;

CREATE TABLE `mlsql_backend_proxy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mlsql_group_id` int(11) DEFAULT NULL,
  `backend_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `backend_name` (`backend_name`),
  KEY `mlsql_group_id` (`mlsql_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table mlsql_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mlsql_group`;

CREATE TABLE `mlsql_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table mlsql_group_role
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mlsql_group_role`;

CREATE TABLE `mlsql_group_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `mlsql_group_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `mlsql_group_id` (`mlsql_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table mlsql_group_role_auth
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mlsql_group_role_auth`;

CREATE TABLE `mlsql_group_role_auth` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mlsql_table_id` int(11) DEFAULT NULL,
  `mlsql_group_role_id` int(11) DEFAULT NULL,
  `operate_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mlsql_table_id` (`mlsql_table_id`),
  KEY `mlsql_group_role_id` (`mlsql_group_role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table mlsql_group_table
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mlsql_group_table`;

CREATE TABLE `mlsql_group_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mlsql_table_id` int(11) DEFAULT NULL,
  `mlsql_group_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mlsql_table_id` (`mlsql_table_id`),
  KEY `mlsql_group_id` (`mlsql_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table mlsql_group_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mlsql_group_user`;

CREATE TABLE `mlsql_group_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mlsql_group_id` int(11) DEFAULT NULL,
  `mlsql_user_id` int(11) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mlsql_group_id` (`mlsql_group_id`),
  KEY `mlsql_user_id` (`mlsql_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table mlsql_role_member
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mlsql_role_member`;

CREATE TABLE `mlsql_role_member` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mlsql_user_id` int(11) DEFAULT NULL,
  `mlsql_group_role_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mlsql_user_id` (`mlsql_user_id`),
  KEY `mlsql_group_role_id` (`mlsql_group_role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table mlsql_table
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mlsql_table`;

CREATE TABLE `mlsql_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `db` varchar(255) DEFAULT NULL,
  `table_type` varchar(255) DEFAULT NULL,
  `source_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table mlsql_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mlsql_user`;

CREATE TABLE `mlsql_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `backend_tags` text,
  `role` text,
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table script_file
# ------------------------------------------------------------

DROP TABLE IF EXISTS `script_file`;

CREATE TABLE `script_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `has_caret` int(11) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `is_dir` int(11) DEFAULT NULL,
  `content` text,
  `is_expanded` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table script_user_rw
# ------------------------------------------------------------

DROP TABLE IF EXISTS `script_user_rw`;

CREATE TABLE `script_user_rw` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `script_file_id` int(11) DEFAULT NULL,
  `mlsql_user_id` int(11) DEFAULT NULL,
  `is_owner` int(11) DEFAULT NULL,
  `readable` int(11) DEFAULT NULL,
  `writable` int(11) DEFAULT NULL,
  `is_delete` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `script_file_id` (`script_file_id`),
  KEY `mlsql_user_id` (`mlsql_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE mlsql_user
  ADD COLUMN status VARCHAR(60) AFTER role;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
