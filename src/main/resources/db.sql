CREATE TABLE script_file
(
  id          int(11) NOT NULL AUTO_INCREMENT,
  name        varchar(255) DEFAULT NULL,
  has_caret   int(11),
  is_expanded int(11),
  icon        varchar(255) DEFAULT NULL,
  label       varchar(255) DEFAULT NULL,
  parent_id   int(11),
  is_dir      int(11),
  content     text         DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE script_user_rw
(
  id             int(11) NOT NULL AUTO_INCREMENT,
  script_file_id int(11),
  mlsql_user_id  int(11),
  is_owner       int(11),
  readable       int(11),
  writable       int(11),
  is_delete      int(11),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE mlsql_user
(
  id           int(11) NOT NULL AUTO_INCREMENT,
  name         varchar(255) DEFAULT NULL,
  password     varchar(255) DEFAULT NULL,
  backend_tags text,
  role         text,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE access_token
(
  id            int(11) NOT NULL AUTO_INCREMENT,
  name          varchar(255) DEFAULT NULL,
  mlsql_user_id int(11),
  create_at     bigint(20),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE aliyun_cluster_process
(
  id            int(11) NOT NULL AUTO_INCREMENT,
  mlsql_user_id int(11),
  status        int(11),
  start_time    text,
  end_time      text,
  reason        text,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- auth related

CREATE TABLE mlsql_group
(
  id   int(11) NOT NULL AUTO_INCREMENT,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE mlsql_group_user
(
  id             int(11) NOT NULL AUTO_INCREMENT,
  mlsql_group_id int(11),
  mlsql_user_id  int(11),
  status         int(11),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE mlsql_group_role
(
  id             int(11) NOT NULL AUTO_INCREMENT,
  name           varchar(255) DEFAULT NULL,
  mlsql_group_id int(11),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE mlsql_table
(
  id          int(11) NOT NULL AUTO_INCREMENT,
  name        varchar(255) DEFAULT NULL,
  db          varchar(255) DEFAULT NULL,
  table_type  varchar(255) DEFAULT NULL,
  source_type varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE mlsql_group_table
(
  id             int(11) NOT NULL AUTO_INCREMENT,
  mlsql_table_id int(11),
  mlsql_group_id int(11),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE mlsql_group_role_auth
(
  id                  int(11) NOT NULL AUTO_INCREMENT,
  mlsql_table_id      int(11),
  mlsql_group_role_id int(11),
  operate_type        varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE mlsql_role_member
(
  id                  int(11) NOT NULL AUTO_INCREMENT,
  mlsql_user_id       int(11),
  mlsql_group_role_id int(11),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE mlsql_backend_proxy
(
  id             int(11) NOT NULL AUTO_INCREMENT,
  mlsql_group_id int(11),
  backend_name   varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




