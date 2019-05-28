--MFCR-------------------------------------------------------------------------
INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES ('ministry', 'Ministerstvo financí ČR', '00006947', 'CZ00006947', TRUE);

-- MFCR: DataInstances must be checked every time extraction runs
INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo financí ČR'),
  'order', 'daily', 'eu.profinit.opendata.institution.mfcr.MFCRHandler', TRUE, 'Objednávky MFČR');

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo financí ČR'),
  'invoice', 'daily', 'eu.profinit.opendata.institution.mfcr.MFCRHandler', TRUE, 'Faktury MFČR');

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo financí ČR'),
  'contract', 'daily', 'eu.profinit.opendata.institution.mfcr.MFCRHandler', TRUE, 'Smlouvy MFČR');

-- Justice: Manual data instance for contracts, others are automatic----------------------------
WITH msp AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo spravedlnosti ČR', '00025429', TRUE) RETURNING entity_id),

dsid AS (
  INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM msp),
  'contract', 'aperiodic', 'eu.profinit.opendata.institution.justice.JusticeHandler', TRUE, 'Smlouvy MSp')
  RETURNING data_source_id
),

dsid2 AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
    (SELECT entity_id FROM msp),
    'invoice', 'quarterly', 'eu.profinit.opendata.institution.justice.JusticeHandler', TRUE, 'Faktury MSp')
    RETURNING data_source_id
)

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES (
  (SELECT  data_source_id FROM dsid), 'https://data.justice.cz/Personln%20informace/Smlouvy%20MSp%20ke%20sta%C5%BEen%C3%AD.xlsx',
  'xlsx', 'quarterly', 'Smlouvy MSp 2011 - 2015', 'mappings/justice/mapping-contracts.xml', false
);

-- MZP: All data instances are manual----------------------------

WITH mzp AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo životního prostředí', '00164801', TRUE) RETURNING entity_id),

    orders_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mzp),
      'order', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Objednávky MŽP')
    RETURNING data_source_id
  ),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mzp),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy MŽP')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mzp),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MŽP')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES
  (
    (SELECT  data_source_id FROM orders_ds), 'https://www.mzp.cz/www/smlouvy-web.nsf/exportOrdersValidAsXLSX.xsp',
    'xlsx', 'weekly', 'Průběžné objednávky MŽP', 'mappings/mzp/mapping-orders.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM contracts_ds), 'https://www.mzp.cz/www/smlouvy-web.nsf/exportContractsValidAsXLSX.xsp',
    'xlsx', 'weekly', 'Platné smlouvy MŽP', 'mappings/mzp/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'https://www.mzp.cz/www/smlouvy-web.nsf/exportInvoicesAsXLSX.xsp',
    'xlsx', 'weekly', 'Průběžné faktury MŽP do 2016', 'mappings/mzp/mapping-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'https://www.mzp.cz/opendata/mzp-faktury_2016.csv',
    'csv', 'weekly', 'Průběžné faktury MŽP od 2016', 'mappings/mzp/mapping-csv-invoices.xml', FALSE
  );

-- MK: Data instances are manual and must be periodically updated ----------------------------

WITH mk AS (INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES
  ('ministry', 'Ministerstvo kultury ČR', '00023671', 'CZ00023671', TRUE) RETURNING entity_id),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mk),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy MK')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mk),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MK')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM contracts_ds), 'https://www.mkcr.cz/assets/povinne-zverejnovane-informace/MK-smlouvy-2015-03-16.xlsx',
    'xlsx', 'aperiodic', 'Platné smlouvy MK k 31. 1. 2015', 'mappings/mk/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'https://www.mkcr.cz/assets/povinne-zverejnovane-informace/Uhrazene-faktury-dobropisy-a-platebni-poukazy_leden_2015.xlsx',
    'xlsx', 'aperiodic', 'Faktury MK leden 2015', 'mappings/mk/mapping-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/5505b49c-1bdd-44d4-87ea-e438fa3da86d',
    'csv', 'aperiodic', 'MK Faktury 2010', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/90a1e97b-b391-4689-b2d8-6d28d2d8c416',
    'csv', 'aperiodic', 'MK Zalohove faktury 2010', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/905a2e36-6376-43dc-bee8-b9c88383b996',
    'csv', 'aperiodic', 'MK Platebni poukazy 2010', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/d051461e-6868-4f39-abf0-d9e5cb6132eb',
    'csv', 'aperiodic', 'MK Dobropisy 2010', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/abf77b13-2d0e-4526-8c43-0f7356ca82f8',
    'csv', 'aperiodic', 'MK Faktury 2011', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/8061bf6e-d37b-48c4-9d02-853ec8449970',
    'csv', 'aperiodic', 'MK Zalohove faktury 2011', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/0671a2a2-161f-40e8-9c10-2d1a13ea95e4',
    'csv', 'aperiodic', 'MK Platebni poukazy 2011', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/c695dc33-2255-49a0-9c9e-bcb491df8d2b',
    'csv', 'aperiodic', 'MK Dobropisy 2011', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/4d723d0e-9df4-416a-940b-170db52a4d5c',
    'csv', 'aperiodic', 'MK Faktury 2012', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/5940e3c0-ff9d-4ab8-a702-c4f724fedb01',
    'csv', 'aperiodic', 'MK Zalohove faktury 2012', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/6c579e50-1924-49e6-a1c1-92c1fb7356b8',
    'csv', 'aperiodic', 'MK Platebni poukazy 2012', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/94b84ad3-4b40-43be-862b-e4a48780ace3',
    'csv', 'aperiodic', 'MK Dobropisy 2012', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/3df5d757-db60-466e-87fd-225f5f9563db',
    'csv', 'aperiodic', 'MK Faktury 2013', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/ac55a50e-5eeb-4931-b6d3-143257b160ec',
    'csv', 'aperiodic', 'MK Zalohove faktury 2013', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/d0601c9e-407e-439d-9141-5050d35f4c3b',
    'csv', 'aperiodic', 'MK Platebni poukazy 2013', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/8664a69a-c8e7-45df-9fb0-0cb3f6fc9081',
    'csv', 'aperiodic', 'MK Dobropisy 2013', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/ebbb77fc-9178-45b4-9413-0558054a51ca',
    'csv', 'aperiodic', 'MK Faktury 2014', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/e35d4d48-14cb-400f-8b00-88f3a4cf3816',
    'csv', 'aperiodic', 'MK Zalohove faktury 2014', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/5e2dc05e-fe80-4ce4-b293-a2637594b976',
    'csv', 'aperiodic', 'MK Platebni poukazy 2014', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/11773e0e-152b-403a-b981-5f593881cadd',
    'csv', 'aperiodic', 'MK Dobropisy 2014', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/fdbef526-c6fd-4ae0-b0b8-e74d1b249a62',
    'csv', 'aperiodic', 'MK Faktury 2015', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/3e3b2f43-e187-42f8-8859-5c8413eb1703',
    'csv', 'aperiodic', 'MK Zalohove faktury 2015', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/4f9a44a5-d10c-414a-b278-a215a28d34f8',
    'csv', 'aperiodic', 'MK Platebni poukazy 2015', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/a4689c8d-ab3c-424a-b619-529de4f6b657',
    'csv', 'aperiodic', 'MK Dobropisy 2015', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/932c3724-c1c0-446d-9b7e-96879f7c7f44',
    'csv', 'aperiodic', 'MK Faktury 2016', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/48dca890-f205-4d13-9555-08befedce799',
    'csv', 'aperiodic', 'MK Zalohove faktury 2016', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/2f73769f-eebc-491b-a672-8a9405469db3',
    'csv', 'aperiodic', 'MK Platebni poukazy 2016', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/2a50336f-4e5d-4097-8848-94b3c3219b7c',
    'csv', 'aperiodic', 'MK Dobropisy 2016', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/7885b288-7824-4b5f-a495-fa3239447d4e',
    'csv', 'aperiodic', 'MK Faktury 2017', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/23f9366b-8c0f-450a-b629-4f0c67198d47',
    'csv', 'aperiodic', 'MK Zalohove faktury 2017', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/5860c9a0-055f-4d76-b312-047d6a75df61',
    'csv', 'aperiodic', 'MK Platebni poukazy 2017', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/6743b253-7e01-47fe-8fad-5b79f2aa4313',
    'csv', 'aperiodic', 'MK Dobropisy 2017', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/44746c4f-bdea-4561-871a-25f17190a034',
    'csv', 'aperiodic', 'MK Faktury 2018', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/cde5a665-c865-4dd6-a72f-0cc1882708eb',
    'csv', 'aperiodic', 'MK Zalohove faktury 2018', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/44746c4f-bdea-4561-871a-25f17190a034',
    'csv', 'aperiodic', 'MK Platebni poukazy 2018', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'https://data.mkcr.cz/homepage/download-file/06516ad6-d4b1-44c5-9efe-d6358cdc8290',
    'csv', 'aperiodic', 'MK Dobropisy 2018', 'mappings/mk/mapping-csv-invoices.xml', FALSE
  );

-- MMR: Data instances are manual and experimentally periodic, but we don't know how updates are published. ----------------------------

WITH mmr AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo pro místní rozvoj', '66002222', TRUE) RETURNING entity_id),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mmr),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy MMR')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mmr),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MMR')
    RETURNING data_source_id
  ),

    invoices_csv_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mmr),
      'invoice', 'yearly', 'eu.profinit.opendata.institution.mmr.MMRHandler', TRUE, 'Faktury MMR CSV')
    RETURNING data_source_id
  ),

    orders_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mmr),
      'order', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Objednávky MMR')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mmr.cz/getmedia/3418880e-894f-4cff-9e9e-62a172394c85/Smlouvy_1.xlsx',
    'xlsx', 'monthly', 'Smlouvy MMR od 1. 1. 2015', 'mappings/mmr/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mmr.cz/getmedia/30c36454-8062-4649-9b55-fbb89d1c86e5/Faktury-2015_2.xlsx',
    'xlsx', 'monthly', 'Faktury MMR od 1. 1. 2015', 'mappings/mmr/mapping-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM orders_ds), 'http://www.mmr.cz/getmedia/2a2ea9c8-e6d5-46f5-8d7b-f961b8fabbce/Objednavky_2.xlsx',
    'xlsx', 'monthly', 'Objednávky MMR od 1. 1. 2015', 'mappings/mmr/mapping-orders.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://data.mmr.cz/dataset/cc7ef807-36a7-4058-886f-e352d6979026/resource/537dd862-5b5e-4068-8ffb-a4f2270de46b/download/cusersvanlukonedrive-mmrstaene-souboryfaktury_2015.csv',
    'csv', 'monthly', 'Faktury MMR 2015', 'mappings/mmr/mapping-csv-2015-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://data.mmr.cz/dataset/012d9e10-3779-484f-a8f9-ce8445d02141/resource/19691295-09e0-4f2b-8d10-3e36e023d873/download/cusersvanlukonedrive-mmrstaene-souboryfaktury_2016.csv',
    'csv', 'monthly', 'Faktury MMR 2016', 'mappings/mmr/mapping-csv-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://data.mmr.cz/dataset/89717919-5db4-469d-842c-01d4f54b0ed9/resource/922a41d9-9bf2-4ba6-a9b3-0ab48a197077/download/cusersvanlukonedrive-mmrstaene-souboryfaktury_2017.csv',
    'csv', 'monthly', 'Faktury MMR 2017', 'mappings/mmr/mapping-csv-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://data.mmr.cz/dataset/5f2d1e58-f7e5-4c00-af9c-2cdfe9a4965f/resource/0909932c-f6fc-4ca7-9126-f8c79ce0eecc/download/jsdilenedata-pro-katalogoufs___k-uveejnnifaktury_2018-12_20190101.csv',
    'csv', 'monthly', 'Faktury MMR 2018', 'mappings/mmr/mapping-csv-invoices.xml', FALSE
  );

-- MMR SFRB

WITH sfrb AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry-organization', 'Státní fond rozvoje bydlení', '70856788', TRUE) RETURNING entity_id),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM sfrb),
      'invoice', 'monthly', 'eu.profinit.opendata.institution.mmr.sfrb.SFRBHandler', TRUE, 'Faktury MMR SFRB')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://data.mmr.cz/dataset/a18ec18b-5f24-465a-bc7a-292f5629ea73/resource/1cc0fbf0-b851-43f9-a46e-97ef03d8ff78/download/seznam_faktur_do_13_12_2016.csv',
    'csv', 'monthly', 'Faktury MMR SFRB 2016', 'mappings/mmr/sfrb/mapping-2016-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.sfrb.cz/fileadmin/user_upload/Seznam_faktur_2017.csv',
    'csv', 'monthly', 'Faktury MMR SFRB 2017', 'mappings/mmr/sfrb/mapping-2017-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.sfrb.cz/fileadmin/user_upload/Seznam_faktur_2018.csv',
    'csv', 'monthly', 'Faktury MMR SFRB 2018', 'mappings/mmr/sfrb/mapping-2018-invoices.xml', FALSE
  );


-- MMR CZT

WITH czt AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry-organization', 'Agentura CzechTourism', '49277600', TRUE) RETURNING entity_id),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM czt),
      'invoice', 'monthly', 'eu.profinit.opendata.institution.mmr.czt.CZTHandler', TRUE, 'Faktury MMR CZT')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.czechtourism.cz/getmedia/36f7347e-4bb1-4b2f-a0cf-8e3f9c9d8083/CzT_faktury_4.csv.aspx',
    'csv', 'monthly', 'Faktury MMR CZT 2016', 'mappings/mmr/czt/mapping-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.czechtourism.cz/getmedia/b06e9b04-0128-4f97-8195-b4ca0990c565/CzT_faktury_2017_10.csv.aspx',
    'csv', 'monthly', 'Faktury MMR CZT 2017', 'mappings/mmr/czt/mapping-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'https://www.czechtourism.cz/getmedia/7809aee4-a221-4774-bd84-0a44da419570/CzT_faktury_2018.csv.aspx',
    'csv', 'monthly', 'Faktury MMR CZT 2018', 'mappings/mmr/czt/mapping-invoices.xml', FALSE
  );

-- MV: Data instances are manual and experimentally periodic, but we don't know how updates are published. ----------------------------

WITH mv AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo vnitra', '00007064', TRUE) RETURNING entity_id),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mv),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy MV')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mv),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MV')
    RETURNING data_source_id
  ),

    orders_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mv),
      'order', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Objednávky MV')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM contracts_ds), '',
    'xlsx', 'monthly', 'Smlouvy MV od 1. 1. 2013', 'mappings/mvcr/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), '',
    'xlsx', 'monthly', 'Faktury MV od 1. 1. 2013', 'mappings/mvcr/mapping-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM orders_ds), '',
    'xlsx', 'monthly', 'Objednávky MV od 1. 1. 2013', 'mappings/mvcr/mapping-orders.xml', FALSE
  );

-- MPO: Data instances are manual and experimentally periodic, but we don't know how updates are published. ----------------------------

WITH mpo AS (INSERT INTO entity(entity_type, name, ico, is_public) VALUES
  ('ministry', 'Ministerstvo průmyslu a obchodu', '47609109', TRUE) RETURNING entity_id),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mpo),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MPO')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM invoices_ds), 'https://www.mpo.cz/assets/cz/rozcestnik/ministerstvo/otevrena-data/lokalni-katalog/2019/2/faktury.xlsx',
    'xlsx', 'aperiodic', 'Faktury MPO 2013 - 2018', 'mappings/mpo/mapping-invoices.xml', FALSE
  );

--MOCR-------------------------------------------------------------------------
INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES ('ministry', 'Ministerstvo obrany ČR', '60162694', 'CZ60162694', TRUE);

-- MOCR: DataInstances must be checked every time extraction runs

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo obrany ČR'),
  'invoice', 'daily', 'eu.profinit.opendata.institution.mocr.MOCRHandler', TRUE, 'Faktury MOČR');

INSERT INTO data_source(entity_id, record_type, periodicity, handling_class, active, description) VALUES (
  (SELECT entity_id FROM entity WHERE name = 'Ministerstvo obrany ČR'),
  'contract', 'daily', 'eu.profinit.opendata.institution.mocr.MOCRHandler', TRUE, 'Smlouvy MOČR');

-- MDCR: Data instances are manual and experimentally periodic, but we don't know how updates are published. ----------------------------

WITH mdcr AS (INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES
  ('ministry', 'Ministerstvo dopravy', '66003008', 'CZ66003008', TRUE) RETURNING entity_id),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mdcr),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy MDČR')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM mdcr),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury MDČR')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/smlouvy/2015/smlouvy_md_2015.xlsx',
    'xlsx', 'aperiodic', 'Smlouvy MDČR 2015', 'mappings/mdcr/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/smlouvy/2016/smlouvy_md_2016.xlsx',
    'xlsx', 'aperiodic', 'Smlouvy MDČR 2016', 'mappings/mdcr/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/smlouvy/2017/smlouvy_md_2017.xlsx',
    'xlsx', 'monthly', 'Smlouvy MDČR 2017', 'mappings/mdcr/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/smlouvy/2018/smlouvy_md_2018.xlsx',
    'xlsx', 'monthly', 'Smlouvy MDČR 2018', 'mappings/mdcr/mapping-contracts.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/faktury/2015/faktury_md_2015.xlsx',
    'xlsx', 'aperiodic', 'Faktury MDČR 2015', 'mappings/mdcr/mapping-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/faktury/2016/faktury_md_2016.xlsx',
    'xlsx', 'monthly', 'Faktury MDČR 2016', 'mappings/mdcr/mapping-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/faktury/2017/faktury_md_2017.xlsx',
    'xlsx', 'monthly', 'Faktury MDČR 2017', 'mappings/mdcr/mapping-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/faktury/2018/faktury_md_2018.xlsx',
    'xlsx', 'monthly', 'Faktury MDČR 2018', 'mappings/mdcr/mapping-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/faktury/2019/faktury_md_2019.xlsx',
    'xlsx', 'monthly', 'Faktury MDČR 2019', 'mappings/mdcr/mapping-invoices.xml', FALSE
  );

-- SFDI: Data instances are manual and experimentally periodic, but we don't know how updates are published. ----------------------------

WITH sfdi AS (INSERT INTO entity(entity_type, name, ico, dic, is_public) VALUES
  ('ministry', 'Státní fond dopravní indfrastruktury', '70856508', 'CZ70856508', TRUE) RETURNING entity_id),

    contracts_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM sfdi),
      'contract', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Smlouvy SFDI')
    RETURNING data_source_id
  ),

    invoices_ds AS (
    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM sfdi),
      'invoice', 'aperiodic', 'eu.profinit.opendata.control.BlankHandler', TRUE, 'Faktury SFDI')
    RETURNING data_source_id
  )

INSERT INTO data_instance(data_source_id, url, format, periodicity, description, mapping_file, incremental) VALUES

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/smlouvy/2015/smlouvy_sfdi_2015.xlsx',
    'xlsx', 'aperiodic', 'Smlouvy SFDI 2015', 'mappings/sfdi/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/smlouvy/2016/smlouvy_sfdi_2016.xlsx',
    'xlsx', 'aperiodic', 'Smlouvy SFDI 2016', 'mappings/sfdi/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/smlouvy/2017/smlouvy_sfdi_2017.xlsx',
    'xlsx', 'aperiodic', 'Smlouvy SFDI 2017', 'mappings/sfdi/mapping-contracts.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM contracts_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/smlouvy/2018/smlouvy_sfdi_2018.xlsx',
    'xlsx', 'aperiodic', 'Smlouvy SFDI 2018', 'mappings/sfdi/mapping-contracts.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/faktury/2015/faktury_sfdi_2015.xlsx',
    'xlsx', 'aperiodic', 'Faktury SFDI 2015', 'mappings/sfdi/mapping-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/faktury/2016/faktury_sfdi_2016.xlsx',
    'xlsx', 'aperiodic', 'Faktury SFDI 2016', 'mappings/sfdi/mapping-invoices.xml', FALSE
  ),
  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/faktury/2017/faktury_sfdi_2017.xlsx',
    'xlsx', 'monthly', 'Faktury SFDI 2017', 'mappings/sfdi/mapping-invoices.xml', FALSE
  ),

  (
    (SELECT  data_source_id FROM invoices_ds), 'http://www.mdcr.cz/MDCR/media/otevrenadata/faktury/2018/faktury_sfdi_2018.xlsx',
    'xlsx', 'aperiodic', 'Faktury SFDI 2018', 'mappings/sfdi/mapping-invoices.xml', FALSE
  );