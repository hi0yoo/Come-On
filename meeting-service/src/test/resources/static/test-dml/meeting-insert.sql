insert into meeting_code (id, created_date_time, modified_date_time, expired_date, invite_code) values (10, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2050-08-22', 'DG055R');
insert into meeting_image (id, created_date_time, modified_date_time, original_name, stored_name) values (10, '2022-08-15T12:13:59.103', '2022-08-15T12:13:59.103', 'original', 'stored');
insert into meeting (id, created_date_time, modified_date_time, end_date, meeting_code_id, meeting_file_id, start_date, title) values (10, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-07-20', 10, 10, '2022-07-10', 'title1');
insert into user_meeting (id, created_date_time, modified_date_time, image_link, meeting_id, meeting_role, nick_name, user_id) values (10, '2022-08-18T13:44:52.804', '2022-08-18T13:44:52.804', 'link1', 10, 'HOST', 'nickname1', 1);
insert into user_meeting (id, created_date_time, modified_date_time, image_link, meeting_id, meeting_role, nick_name, user_id) values (11, '2022-08-19T13:44:52.804', '2022-08-19T13:44:52.804', 'link2', 10, 'PARTICIPANT', 'nickname2', 2);

insert into meeting_code (id, created_date_time, modified_date_time, expired_date, invite_code) values (11, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2010-08-10', '4G235A');
insert into meeting_image (id, created_date_time, modified_date_time, original_name, stored_name) values (11, '2022-08-15T12:13:59.103', '2022-08-15T12:13:59.103', 'original', 'stored');
insert into meeting (id, created_date_time, modified_date_time, end_date, meeting_code_id, meeting_file_id, start_date, title) values (11, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-08-05', 11, 11, '2022-07-15', 'title2');
insert into user_meeting (id, created_date_time, modified_date_time, image_link, meeting_id, meeting_role, nick_name, user_id) values (13, '2022-08-18T13:44:52.804', '2022-08-18T13:44:52.804', 'link3', 11, 'HOST', 'nickname3', 1);

insert into meeting_code (id, created_date_time, modified_date_time, expired_date, invite_code) values (12, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-08-22', '910CSB');
insert into meeting_image (id, created_date_time, modified_date_time, original_name, stored_name) values (12, '2022-08-15T12:13:59.103', '2022-08-15T12:13:59.103', 'original', 'stored');
insert into meeting (id, created_date_time, modified_date_time, end_date, meeting_code_id, meeting_file_id, start_date, title) values (12, '2022-08-15T12:13:59.101', '2022-08-15T12:13:59.101', '2022-08-10', 12, 12, '2022-07-30', 'title3');
insert into user_meeting (id, created_date_time, modified_date_time, image_link, meeting_id, meeting_role, nick_name, user_id) values (14, '2022-08-18T13:44:52.804', '2022-08-18T13:44:52.804', 'link4', 12, 'HOST', 'nickname4', 1);

insert into meeting_place (id, created_date_time, modified_date_time, lat, lng, meeting_id, memo, name, orders) values (10, '2022-08-18T13:44:52.860', '2022-08-18T13:44:52.860', 1.1, 1.1, 10, 'memo1', 'name1', 3);
insert into meeting_place (id, created_date_time, modified_date_time, lat, lng, meeting_id, memo, name, orders) values (11, '2022-08-18T13:44:52.863', '2022-08-18T13:44:52.863', 2.1, 2.1, 10, 'memo2', 'name2', 2);
insert into meeting_place (id, created_date_time, modified_date_time, lat, lng, meeting_id, memo, name, orders) values (12, '2022-08-18T13:44:52.865', '2022-08-18T13:44:52.865', 3.1, 3.1, 10, 'memo3', 'name3', 1);

insert into meeting_date (id, created_date_time, modified_date_time, date, meeting_id, user_count, date_status) values (10, '2022-08-18T13:44:52.860', '2022-08-18T13:44:52.860', '2022-07-20', 10, 1, 'UNFIXED');
insert into meeting_date (id, created_date_time, modified_date_time, date, meeting_id, user_count, date_status) values (11, '2022-08-18T13:44:52.863', '2022-08-18T13:44:52.863', '2022-07-15', 10, 2, 'UNFIXED');
insert into meeting_date (id, created_date_time, modified_date_time, date, meeting_id, user_count, date_status) values (12, '2022-08-18T13:44:52.865', '2022-08-18T13:44:52.865', '2022-07-10', 10, 1, 'UNFIXED');

insert into date_user (id, meeting_date_id, meeting_user_id) values (default, 10, 11);