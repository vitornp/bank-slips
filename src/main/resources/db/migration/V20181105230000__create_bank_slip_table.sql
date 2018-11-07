CREATE TABLE bank_slip
(
    id             uuid                     not null,
    due_date       date                     not null,
    total_in_cents decimal(10, 2)           not null,
    costumer       text                     not null,
    status         text                     not null,
    created_at     timestamp with time zone not null,
    CONSTRAINT payment_pkey PRIMARY KEY (id)
)
