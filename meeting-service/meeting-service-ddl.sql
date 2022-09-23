create table meeting_code
(
    id                 bigint auto_increment,
    created_date_time  datetime(6)  not null,
    modified_date_time datetime(6)  not null,
    expired_date       date         not null,
    invite_code        varchar(30)  not null,
    constraint code_id_pk primary key (id)
);

create table meeting_image
(
    id                 bigint auto_increment,
    created_date_time  datetime(6)  not null,
    modified_date_time datetime(6)  not null,
    original_name      varchar(255) not null,
    stored_name        varchar(255) not null,
    constraint image_id_pk primary key (id)
);

create table meeting
(
    id                 bigint auto_increment,
    created_date_time  datetime(6)  not null,
    modified_date_time datetime(6)  not null,
    end_date           date         not null,
    start_date         date         not null,
    title              varchar(255) not null,
    meeting_code_id    bigint       not null,
    meeting_file_id    bigint       not null,
    constraint meeting_id_pk primary key (id),
    constraint meeting_file_id_uq unique (meeting_file_id),
    constraint meeting_code_id_uq unique (meeting_code_id),
    constraint meeting_file_id_fk foreign key (meeting_file_id) references meeting_image (id),
    constraint meeting_code_id_fk foreign key (meeting_code_id) references meeting_code (id)
);

create table meeting_date
(
    id                 bigint auto_increment,
    created_date_time  datetime(6)  null,
    modified_date_time datetime(6)  null,
    date               date         not null,
    date_status        varchar(30)  not null,
    user_count         int          not null,
    meeting_id         bigint       null,
    constraint date_id_pk primary key (id),
    constraint date_date_uq unique (date),
    constraint date_meeting_id_fk foreign key (meeting_id) references meeting (id) on delete cascade
);

create table meeting_place
(
    id                 bigint auto_increment,
    created_date_time  datetime(6)   not null,
    modified_date_time datetime(6)   not null,
    api_id             bigint        not null,
    category           varchar(30)   not null,
    lat                double        not null,
    lng                double        not null,
    memo               varchar(1000) null,
    name               varchar(255)  not null,
    orders             int           not null,
    meeting_id         bigint        not null,
    constraint place_id_pk primary key (id),
    constraint place_meeting_id_fk foreign key (meeting_id) references meeting (id) on delete cascade
);

create table user_meeting
(
    id                 bigint auto_increment,
    created_date_time  datetime(6)  not null,
    modified_date_time datetime(6)  not null,
    meeting_role       varchar(30)  not null,
    user_id            bigint       not null,
    meeting_id         bigint       not null,
    constraint mu_id_pk primary key (id),
    constraint mu_meeting_id_fk foreign key (meeting_id) references meeting (id) on delete cascade
);

create table date_user
(
    id                 bigint auto_increment,
    created_date_time  datetime(6) not null,
    modified_date_time datetime(6) not null,
    meeting_date_id    bigint      not null,
    meeting_user_id    bigint      not null,
    constraint du_id_pk primary key (id),
    constraint du_user_id_fk foreign key (meeting_user_id) references user_meeting (id) on delete cascade,
    constraint du_date_id_fk foreign key (meeting_date_id) references meeting_date (id) on delete cascade
);