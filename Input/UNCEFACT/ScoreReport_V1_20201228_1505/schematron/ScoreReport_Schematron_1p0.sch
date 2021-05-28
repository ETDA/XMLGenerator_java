<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
    xmlns:sqf="http://www.schematron-quickfix.com/validator/process">
    <sch:ns uri="urn:un:unece:etda:data:standard:ScoreReport:1" prefix="rsm"/>
    
    <!-- Check Document Type Code -->
    <sch:pattern>
        <sch:rule context="ScoreReport">
            <sch:report test="not(ScoreReport)"
                >ไม่ผ่านการตรวจสอบเงื่อนไขที่กำหนด (Schematron)
                เนื่องจากประเภทของเอกสารที่เลือกไม่ตรงกับเอกสารที่นำมาตรวจสอบ</sch:report>
        </sch:rule>
    </sch:pattern>
</sch:schema>