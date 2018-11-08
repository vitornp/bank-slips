CREATE TABLE bank_slip_status
(
    id             uuid                     not null,
    bank_slip_id   uuid                     not null,
    status         text                     not null,
    date           date                     not null,
    created_at     timestamp with time zone not null,
    CONSTRAINT bank_slip_status_pkey PRIMARY KEY (id)
)
