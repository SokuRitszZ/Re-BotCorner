create table rebc.b_group
(
    id          int unsigned auto_increment comment '主键'
        primary key,
    title       varchar(32)   null comment '小组名',
    icon        varchar(1000) null comment '图标',
    description varchar(256)  null comment '描述',
    create_time datetime      null comment '创建时间',
    creator_id  int unsigned  not null,
    constraint id
        unique (id),
    constraint b_group_user_id_fk
        foreign key (creator_id) references rebc.user (id)
)
    comment '小组';

