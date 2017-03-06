CREATE TABLE items (
  id          SERIAL   NOT NULL PRIMARY KEY,
  title       TEXT 	   NOT NULL,
  done	      BOOLEAN  NOT NULL,
  create_date TEXT     NOT NULL,
  note        TEXT     NULL,
  start_time  TEXT     NULL,
  end_time    TEXT     NULL
);