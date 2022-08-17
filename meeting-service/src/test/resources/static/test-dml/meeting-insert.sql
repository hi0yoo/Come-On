insert into meeting_code (id, created_date_time, modified_date_time, expired_date, invite_code) values (10, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-08-22', 'DG055R');
insert into meeting_image (id, created_date_time, modified_date_time, original_name, stored_name) values (10, '2022-08-15T12:13:59.103', '2022-08-15T12:13:59.103', 'original', 'stored');
insert into meeting (id, created_date_time, modified_date_time, end_date, meeting_code_id, meeting_file_id, start_date, title) values (10, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-07-20', 10, 10, '2022-07-10', 'title1');
insert into user_meeting (id, created_date_time, modified_date_time, meeting_id, meeting_role, user_id) values (default, '2022-08-15T12:13:59.121', '2022-08-15T12:13:59.121', 10, 'HOST', 1);

insert into meeting_code (id, created_date_time, modified_date_time, expired_date, invite_code) values (11, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-08-22', '4G235A');
insert into meeting_image (id, created_date_time, modified_date_time, original_name, stored_name) values (11, '2022-08-15T12:13:59.103', '2022-08-15T12:13:59.103', 'original', 'stored');
insert into meeting (id, created_date_time, modified_date_time, end_date, meeting_code_id, meeting_file_id, start_date, title) values (11, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-08-05', 11, 11, '2022-07-15', 'title2');
insert into user_meeting (id, created_date_time, modified_date_time, meeting_id, meeting_role, user_id) values (default, '2022-08-15T12:13:59.121', '2022-08-15T12:13:59.121', 11, 'HOST', 1);

insert into meeting_code (id, created_date_time, modified_date_time, expired_date, invite_code) values (12, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-08-22', '910CSB');
insert into meeting_image (id, created_date_time, modified_date_time, original_name, stored_name) values (12, '2022-08-15T12:13:59.103', '2022-08-15T12:13:59.103', 'original', 'stored');
insert into meeting (id, created_date_time, modified_date_time, end_date, meeting_code_id, meeting_file_id, start_date, title) values (12, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-08-10', 12, 12, '2022-07-30', 'title3');
insert into user_meeting (id, created_date_time, modified_date_time, meeting_id, meeting_role, user_id) values (default, '2022-08-15T12:13:59.121', '2022-08-15T12:13:59.121', 12, 'HOST', 1);