/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 19-XI-2015 9:54:06 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Drop Sequences for Autonumber Columns */

DROP SEQUENCE IF EXISTS "retrieval_retrieval_id_seq"
;

DROP SEQUENCE IF EXISTS "record_record_id_seq"
;

DROP SEQUENCE IF EXISTS "entity_entity_id_seq"
;

DROP SEQUENCE IF EXISTS "data_source_data_source_id_seq"
;

DROP SEQUENCE IF EXISTS "data_instance_data_instance_id_seq"
;

DROP SEQUENCE IF EXISTS "data_instance_data_source_id_seq"
;

DROP SEQUENCE IF EXISTS "partner_list_entry_partner_list_entry_id_seq"
;

DROP SEQUENCE IF EXISTS "unresolved_relationship_unresolved_relationship_id_seq"
;

/* Drop Tables */

DROP TABLE IF EXISTS "retrieval" CASCADE
;

DROP TABLE IF EXISTS "record_type" CASCADE
;

DROP TABLE IF EXISTS "record" CASCADE
;

DROP TABLE IF EXISTS "periodicity" CASCADE
;

DROP TABLE IF EXISTS "entity_type" CASCADE
;

DROP TABLE IF EXISTS "entity" CASCADE
;

DROP TABLE IF EXISTS "data_source" CASCADE
;

DROP TABLE IF EXISTS "data_instance" CASCADE
;

DROP TABLE IF EXISTS "authority_role" CASCADE
;

DROP TABLE IF EXISTS "partner_list_entry" CASCADE
;

DROP TABLE IF EXISTS "unresolved_relationship" CASCADE
;

/* Create Tables */

CREATE TABLE "retrieval"
(
	"date" timestamp without time zone NOT NULL,
	"failure_reason" varchar(1000)	 NULL,
	"num_bad_records" int NOT NULL,
	"num_records_inserted" int NOT NULL,
	"success" boolean NOT NULL,
	"retrieval_id" integer NOT NULL DEFAULT nextval(('"retrieval_retrieval_id_seq"'::text)::regclass),
	"data_instance_id" integer NULL
)
;

CREATE TABLE "record_type"
(
	"record_type" varchar(50)	 NOT NULL
)
;

CREATE TABLE "record"
(
	"amount_czk" double precision NULL,
	"authority_identifier" varchar(50)	 NULL,
	"currency" varchar(3)	 NOT NULL,
	"date_created" date NOT NULL,
	"date_of_expiry" date NULL,
	"date_of_payment" date NULL,
	"due_date" date NULL,
	"in_effect" boolean NULL,
	"master_id" varchar(50)	 NOT NULL,
	"original_currency_amount" double precision NULL,
	"subject" varchar(5000)	 NULL,
	"variable_symbol" varchar(50)	 NULL,
	"record_id" integer NOT NULL DEFAULT nextval(('"record_record_id_seq"'::text)::regclass),
	"partner" integer NULL,
	"authority" integer NULL,
	"parent_id" integer NULL,
	"retrieval_id" integer NULL,
	"record_type" varchar(50)	 NOT NULL,
	"authority_role" varchar(50)	 NULL,
	"budget_category" varchar(500) NULL,
	"periodicity" VARCHAR(50) NULL
)
;

CREATE TABLE "periodicity"
(
	"periodicity" varchar(50)	 NOT NULL
)
;

CREATE TABLE "entity_type"
(
	"entity_type" varchar(50)	 NOT NULL
)
;

CREATE TABLE "entity"
(
	"dic" varchar(50)	 NULL,
	"ico" varchar(50)	 NULL,
	"is_public" boolean NOT NULL,
	"name" varchar(2000)	 NOT NULL,
	"entity_id" integer NOT NULL DEFAULT nextval(('"entity_entity_id_seq"'::text)::regclass),
	"entity_type" varchar(50)	 NOT NULL
)
;

CREATE TABLE "data_source"
(
	"last_processed_date" timestamp without time zone NULL,
	"data_source_id" integer NOT NULL DEFAULT nextval(('"data_source_data_source_id_seq"'::text)::regclass),
	"entity_id" integer NOT NULL,
	"record_type" varchar(50)	 NOT NULL,
	"periodicity" varchar(50)	 NOT NULL,
	"handling_class" varchar(100)	 NULL,
	"active" boolean NOT NULL,
	"description" varchar(255)	 NULL
)
;

CREATE TABLE "data_instance"
(
	"format" varchar(6)	 NOT NULL,
	"url" varchar(255)	 NOT NULL,
	"data_instance_id" integer NOT NULL DEFAULT nextval(('"data_instance_data_instance_id_seq"'::text)::regclass),
	"data_source_id" integer NOT NULL DEFAULT nextval(('"data_instance_data_source_id_seq"'::text)::regclass),
	"periodicity" varchar(50)	 NOT NULL,
	"last_processed_date" timestamp without time zone NULL,
	"expires" date NULL,
	"last_processed_row" integer NULL,
	"authority_id" varchar(255) NULL,
	"description" varchar(255) NULL,
	"mapping_file" varchar(255) NULL,
	"incremental" boolean NOT NULL DEFAULT true
)
;

CREATE TABLE "authority_role"
(
	"authority_role" varchar(50)	 NOT NULL
)
;

CREATE TABLE "partner_list_entry"
(
	"authority_id" integer NOT NULL,
	"partner_id" integer NOT NULL,
	"code" varchar(50)	 NOT NULL,
	"partner_list_entry_id" integer NOT NULL DEFAULT nextval(('"partner_list_entry_partner_list_entry_id_seq"'::text)::regclass)
)
;

CREATE TABLE "unresolved_relationship"
(
	"bound_authority_identifier" varchar(50)	 NOT NULL,
	"saved_record_id" integer NOT NULL,
	"unresolved_relationship_id" integer NOT NULL DEFAULT nextval(('"unresolved_relationship_unresolved_relationship_id_seq"'::text)::regclass),
	"saved_record_is_parent" boolean NOT NULL,
	"record_type" varchar(50) NULL
)
;

/* Create Table Comments, Sequences for Autonumber Columns */

CREATE SEQUENCE "retrieval_retrieval_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "record_record_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "entity_entity_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "data_source_data_source_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "data_instance_data_instance_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "data_instance_data_source_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "partner_list_entry_partner_list_entry_id_seq" INCREMENT 1 START 1
;

CREATE SEQUENCE "unresolved_relationship_unresolved_relationship_id_seq" INCREMENT 1 START 1
;


/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "retrieval" ADD CONSTRAINT "PK_retrieval"
	PRIMARY KEY ("retrieval_id")
;

ALTER TABLE "record_type" ADD CONSTRAINT "PK_record_type"
	PRIMARY KEY ("record_type")
;

ALTER TABLE "record" ADD CONSTRAINT "PK_record"
	PRIMARY KEY ("record_id")
;

ALTER TABLE "periodicity" ADD CONSTRAINT "PK_Periodicity"
	PRIMARY KEY ("periodicity")
;

ALTER TABLE "entity_type" ADD CONSTRAINT "PK_entity_type"
	PRIMARY KEY ("entity_type")
;

ALTER TABLE "entity" ADD CONSTRAINT "PK_entity"
	PRIMARY KEY ("entity_id")
;

ALTER TABLE "data_source" ADD CONSTRAINT "PK_data_source"
	PRIMARY KEY ("data_source_id")
;

ALTER TABLE "data_instance" ADD CONSTRAINT "PK_data_instance"
	PRIMARY KEY ("data_instance_id")
;

ALTER TABLE "authority_role" ADD CONSTRAINT "PK_authority_role"
	PRIMARY KEY ("authority_role")
;

ALTER TABLE "partner_list_entry" ADD CONSTRAINT "PK_PartnerListEntry"
	PRIMARY KEY ("partner_list_entry_id")
;

ALTER TABLE "unresolved_relationship" ADD CONSTRAINT "PK_unresolved_relationship"
PRIMARY KEY ("unresolved_relationship_id")
;


/* Create Foreign Key Constraints */

ALTER TABLE "retrieval" ADD CONSTRAINT "FK_retrieval_data_instance"
	FOREIGN KEY ("data_instance_id") REFERENCES "data_instance" ("data_instance_id") ON DELETE Cascade ON UPDATE Cascade
;

ALTER TABLE "record" ADD CONSTRAINT "FK_record_authority_role"
	FOREIGN KEY ("authority_role") REFERENCES "authority_role" ("authority_role") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "record" ADD CONSTRAINT "FK_record_record_type"
	FOREIGN KEY ("record_type") REFERENCES "record_type" ("record_type") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "record" ADD CONSTRAINT "FK_partner"
	FOREIGN KEY ("partner") REFERENCES "entity" ("entity_id") ON DELETE Set Null ON UPDATE Cascade
;

ALTER TABLE "record" ADD CONSTRAINT "FK_authority"
	FOREIGN KEY ("authority") REFERENCES "entity" ("entity_id") ON DELETE Set Null ON UPDATE Cascade
;

ALTER TABLE "record" ADD CONSTRAINT "FK_record_parent"
	FOREIGN KEY ("parent_id") REFERENCES "record" ("record_id") ON DELETE Set Null ON UPDATE Cascade
;

ALTER TABLE "record" ADD CONSTRAINT "FK_record_retrieval"
	FOREIGN KEY ("retrieval_id") REFERENCES "retrieval" ("retrieval_id") ON DELETE Cascade ON UPDATE Cascade
;

ALTER TABLE "entity" ADD CONSTRAINT "FK_entity_entity_type"
	FOREIGN KEY ("entity_type") REFERENCES "entity_type" ("entity_type") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "data_source" ADD CONSTRAINT "FK_data_source_periodicity"
	FOREIGN KEY ("periodicity") REFERENCES "periodicity" ("periodicity") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "data_source" ADD CONSTRAINT "FK_data_source_record_type"
	FOREIGN KEY ("record_type") REFERENCES "record_type" ("record_type") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "data_source" ADD CONSTRAINT "FK_data_source_entity"
	FOREIGN KEY ("entity_id") REFERENCES "entity" ("entity_id") ON DELETE Cascade ON UPDATE Cascade
;

ALTER TABLE "data_instance" ADD CONSTRAINT "FK_data_instance_periodicity"
	FOREIGN KEY ("periodicity") REFERENCES "periodicity" ("periodicity") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "data_instance" ADD CONSTRAINT "FK_data_instance_data_source"
	FOREIGN KEY ("data_source_id") REFERENCES "data_source" ("data_source_id") ON DELETE Cascade ON UPDATE Cascade
;

ALTER TABLE "partner_list_entry" ADD CONSTRAINT "FK_partner_list_entry_entity"
	FOREIGN KEY ("authority_id") REFERENCES "entity" ("entity_id") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "partner_list_entry" ADD CONSTRAINT "FK_partner_list_entry_entity_02"
	FOREIGN KEY ("partner_id") REFERENCES "entity" ("entity_id") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "unresolved_relationship" ADD CONSTRAINT "FK_unresolved_relationship_record"
FOREIGN KEY ("saved_record_id") REFERENCES "record" ("record_id") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "unresolved_relationship" ADD CONSTRAINT "FK_unresolved_relationship_record_type"
FOREIGN KEY ("record_type") REFERENCES "record_type" ("record_type") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "record" ADD CONSTRAINT "FK_record_periodicity"
FOREIGN KEY ("periodicity") REFERENCES "periodicity" ("periodicity") ON DELETE No Action ON UPDATE No Action
;

-- Indexes

CREATE INDEX auth_id_idx ON record USING hash(authority_identifier);

-- Comments

COMMENT ON COLUMN retrieval.date IS 'The time the retrieval was started';
COMMENT ON COLUMN retrieval.failure_reason IS 'The reason for a total failure, if there was one.';
COMMENT ON COLUMN retrieval.num_bad_records IS 'The number of records that haven''t been inserted due to a non-fatal error during processing.';
COMMENT ON COLUMN retrieval.num_records_inserted IS 'The number of records successfully inserted during processing.';
COMMENT ON COLUMN retrieval.success IS 'Indicates whether this Retrieval has finished successfully.';
COMMENT ON COLUMN retrieval.retrieval_id IS 'Primary key';
COMMENT ON COLUMN retrieval.data_instance_id IS 'The data instance on which the Retrieval was attempted.';

COMMENT ON COLUMN record.amount_czk IS 'The amount in CZK. Only present when the currency is CZK or a converted amount is available in the source document.';
COMMENT ON COLUMN record.authority_identifier IS 'The identifier that the publishing authority has given to this record. May not be unique even among records from the same authority.';
COMMENT ON COLUMN record.currency IS 'The transaction currency. When there is no indication in the published document, CZK is assumed by default.';
COMMENT ON COLUMN record.date_created IS 'For invoices: usually the date the invoice was received. For orders: the day the order was placed. For contracts: the day the contract came into effect.';
COMMENT ON COLUMN record.date_of_expiry IS 'Only applicable to contracts. The date when the contract expires.';
COMMENT ON COLUMN record.date_of_payment IS 'Only applicable to invoices. The date the invoice was physically paid.';
COMMENT ON COLUMN record.due_date IS 'Only applicable to invoices. The due date for payment of the invoice.';
COMMENT ON COLUMN record.in_effect IS 'Only applicable to contracts. Indicates whether the contract was in effect as of the last processing of the published data source.';
COMMENT ON COLUMN record.master_id IS 'Used to correlate records that represent the same transaction but published by two different entities or to correlate parts of a record that has been broken down into parts, for example to fit into different budget categories.';
COMMENT ON COLUMN record.original_currency_amount IS 'The amount in the currency of the transaction (includes CZK).';
COMMENT ON COLUMN record.budget_category IS 'A more general description of the subject or a specific budget category, if published.';
COMMENT ON COLUMN record.subject IS 'A description of the record, as published by the authority.';
COMMENT ON COLUMN record.variable_symbol IS 'The variable symbol used for an invoice or order payment.';
COMMENT ON COLUMN record.record_id IS 'Primary key';
COMMENT ON COLUMN record.record_type IS 'One of invoice, order, contract, payment.';
COMMENT ON COLUMN record.authority_role IS 'Customer or supplier';
COMMENT ON COLUMN record.retrieval_id IS 'The Retrieval during which this Record was inserted.';
COMMENT ON COLUMN record.authority IS 'The publishing authority.';
COMMENT ON COLUMN record.partner IS 'The partner in this transaction (other side of the transaction from the publishing authority).';
COMMENT ON COLUMN record.periodicity IS 'Only applicable to recurring payments in a contract.';

COMMENT ON COLUMN entity.dic IS 'The tax identification number. Mostly unused.';
COMMENT ON COLUMN entity.ico IS 'The taxpayer identification number. The primary attribute used to avoid duplicitous entities.';
COMMENT ON COLUMN entity.is_public IS 'Indicates that this institution is a public institution that publishes data';
COMMENT ON COLUMN entity.name IS 'The normalized entity name.';
COMMENT ON COLUMN entity.entity_id IS 'Primary key';
COMMENT ON COLUMN entity.entity_type IS 'The nature of this entity.';

COMMENT ON COLUMN data_source.last_processed_date IS 'The time this data source was last processed. This may or may not have involved retrievals.';
COMMENT ON COLUMN data_source.data_source_id IS 'Primary key';
COMMENT ON COLUMN data_source.record_type IS 'The type of record one can expect to get from this DataSource. This does not mean data files cannot contain any others.';
COMMENT ON COLUMN data_source.periodicity IS 'Specifies how often new DataInstances should be generated for this DataSource.';
COMMENT ON COLUMN data_source.entity_id IS 'The public Entity that is publishing this DataSource.';
COMMENT ON COLUMN data_source.description IS 'A description of this DataSource.';
COMMENT ON COLUMN data_source.active IS 'Indicates whether this DataSource should be processed when the application runs.';
COMMENT ON COLUMN data_source.handling_class IS 'The DataSourceHandler that should be invoked when processing this DataSource.';

COMMENT ON COLUMN data_instance.format IS 'XLS or XLSX';
COMMENT ON COLUMN data_instance.last_processed_date IS 'The time of the last successful processing of this DataInstance';
COMMENT ON COLUMN data_instance.url IS 'URL to be used to retrieve the actual XLS file';
COMMENT ON COLUMN data_instance.data_instance_id IS 'Primary key';
COMMENT ON COLUMN data_instance.data_source_id IS 'The parent DataSource';
COMMENT ON COLUMN data_instance.periodicity IS 'How often this DataInstance can be expected to contain new data.';
COMMENT ON COLUMN data_instance.expires IS 'The date after which this DataInstance should no longer be processed.';
COMMENT ON COLUMN data_instance.incremental IS 'Indicates whether the rows that have already been processed previously can be skipped in any new retrieval.';
COMMENT ON COLUMN data_instance.last_processed_row IS 'The row at which the last retrieval has ended. Only applicable if incremental is true.';
COMMENT ON COLUMN data_instance.authority_id IS 'The authority''s own identifier for this DataInstance.';
COMMENT ON COLUMN data_instance.description IS 'Description of the data file''s content';
COMMENT ON COLUMN data_instance.mapping_file IS 'Path to the mapping file that should be used when processing this DataInstance.';

COMMENT ON COLUMN partner_list_entry.authority_id IS 'The authority that has published information about the partner';
COMMENT ON COLUMN partner_list_entry.partner_id IS 'The partner that is published by the authority under an identification code.';
COMMENT ON COLUMN partner_list_entry.code IS 'The identification code used by the authority to refer to the partner.';
COMMENT ON COLUMN partner_list_entry.partner_list_entry_id IS 'Primary key.';

COMMENT ON COLUMN unresolved_relationship.unresolved_relationship_id IS 'Primary key';
COMMENT ON COLUMN unresolved_relationship.saved_record_id IS 'The side of the relationship that is saved in the database.';
COMMENT ON COLUMN unresolved_relationship.bound_authority_identifier IS 'The authority identifier of the record that is to be the other side of the relationship.';
COMMENT ON COLUMN unresolved_relationship.saved_record_is_parent IS 'Indicates which side of the relationship is the parent record.';