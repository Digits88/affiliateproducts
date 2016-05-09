create user 'makeuser'@'localhost' identified by 'make';
create user 'makeuser'@'%' identified by 'make';
create user 'makeuser'@'hfdvsywmkweb1.intra.searshc.com' identified by 'make';
grant all privileges on *.* to 'makeuser'@'hfdvsywmkweb1.intra.searshc.com';
grant all privileges on *.* to 'makeuser'@'localhost';
grant all privileges on *.* to 'makeuser'@'%';

create schema if not exists makedb;
create schema if not exists makedb_test;