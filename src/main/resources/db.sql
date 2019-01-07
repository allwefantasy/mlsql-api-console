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
  id       int(11) NOT NULL AUTO_INCREMENT,
  name     varchar(255) DEFAULT NULL,
  password varchar(255) DEFAULT NULL,
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
  startTime     text,
  endTime       text,
  reason        text,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
