-- ZeladoriaCidadã | Remove as tabelas (use apenas para recriar o banco do zero).
-- Erros ORA-00942 na primeira execução são esperados: as tabelas ainda não existem.
DROP TABLE TB_ZC_ACOMPANHAMENTO CASCADE CONSTRAINTS;
DROP TABLE TB_ZC_OCORRENCIA     CASCADE CONSTRAINTS;
DROP TABLE TB_ZC_REGIAO         CASCADE CONSTRAINTS;
DROP TABLE TB_ZC_CATEGORIA      CASCADE CONSTRAINTS;
DROP TABLE TB_ZC_CIDADAO        CASCADE CONSTRAINTS;
