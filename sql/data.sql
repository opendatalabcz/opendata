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
  ('ministry-organization', 'Státní fond rozvoje bydlení', '70856788', TRUE) RETURNING entity_id)

    INSERT INTO data_source (entity_id, record_type, periodicity, handling_class, active, description) VALUES (
      (SELECT entity_id FROM sfrb),
      'invoice', 'monthly', 'eu.profinit.opendata.institution.mmr.sfrb.SFRBHandler', TRUE, 'Faktury MMR SFRB')

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
    'xlsx', 'aperiodic', 'Smlouvy MPO 2013 - 2018', 'mappings/mpo/mapping-invoices.xml', FALSE
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