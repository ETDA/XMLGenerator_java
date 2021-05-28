# XMLGenerator Java
## Prerequisites
- Java JDK 8 
- Eclipse (Editor สำหรับใช้การพัฒนา)
- Microsoft SQL Server (สามารถเปลี่ยนเป็น Database อื่นได้ โดยจะต้องเปลี่ยนการประกาศ Dependency ในหัวข้อถัดไปด้วย)
## Maven Dependencies
| **#** | **GroupId** | **ArtifactId** | **Version**|
| ------ | ------ | ------ | ------ |
| 1 | com.googlecode.json-simple | json-simple | 1.1.1 |
| 2 | com.fasterxml.jackson.core | jackson-databind | 2.12.0 |
| 3 | org.json | json | 20201115 |
| 4 | com.microsoft.sqlserver | mssql-jdbc | 8.4.1.jre8 |
| 5 | joda-time | joda-time | 2.10.10 |
| 6 | ca.uhn.hapi.fhir | hapi-fhir-structures-r5 | 5.3.0 |
| 7 | ca.uhn.hapi.fhir | hapi-fhir-base | 5.3.0 |
| 8 | ca.uhn.hapi.fhir | hapi-fhir-validation | 5.3.0 |
| 9 | com.squareup.okhttp3 | okhttp | 4.9.0 |
| 10 | org.slf4j | slf4j-simple | 1.7.28 |

## Getting started

การใช้งาน XML Generator Java สามารถศึกษาได้จากคู่มีอการใช้งานดังนี้ /Manual/01_Manual_XMLGenerator (Java).pdf