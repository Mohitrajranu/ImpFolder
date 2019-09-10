const config = require('./Config.json');
const XLSX = require('xlsx');
const fs = require('fs');
var csvWriter = require('csv-write-stream');
////


var unirest = require("unirest");


//  set to write to csv file
var writer = csvWriter({ sendHeaders: false });
var csvFilename = "myfile.csv";
writer.pipe(fs.createWriteStream(csvFilename, { flags: 'a' }));




const workbook = XLSX.readFile(`${config.fileName}`);
const sheet_name_list = workbook.SheetNames;
const excel = XLSX.utils.sheet_to_json(workbook.Sheets[sheet_name_list[0]]);



for (i = 0; i < excel.length; i++) {
   console.log(config.firstNameColumn);
   const firstName = excel[i][config.firstNameColumn] && excel[i][config.firstNameColumn].trim();
   const lastName = excel[i][config.lastNameColumn] && excel[i][config.lastNameColumn].trim();
   const company = excel[i][config.companyNameColumn] && excel[i][config.companyNameColumn].trim();
   config.domains.forEach(domain => {


      if (firstName) {

         let email = buildEmailString([firstName], '', domain);

         // TODO check if email is valid
         // if valid save to csv file


         checkEmailAndSave(email, firstName, lastName, company);
      }


      if (lastName) {

         email = buildEmailString([lastName], '', domain);

         // TODO check if email is valid
         // if valid save to csv file
         checkEmailAndSave(email, firstName, lastName, company);
      }


      if (company) {
         email = buildEmailString([company], '', domain);

         // TODO check if email is valid
         // if valid save to csv file
       
         checkEmailAndSave(email, firstName, lastName, company);
      }




      config.separators.forEach(separator => {



         if (firstName && lastName) {
            let email = buildEmailString([firstName, lastName], separator, domain);

            // TODO check if email is valid
            // if valid save to csv file
            checkEmailAndSave(email, firstName, lastName, company);


            email = buildEmailString([lastName, firstName], separator, domain);


            // TODO check if email is valid
            // if valid save to csv file
            checkEmailAndSave(email, firstName, lastName, company);

            if (company) {
               email = buildEmailString([company, lastName], separator, domain);

               // TODO check if email is valid
               // if valid save to csv file
               checkEmailAndSave(email, firstName, lastName, company);

               email = buildEmailString([lastName, company], separator, domain);


               // TODO check if email is valid
               // if valid save to csv file
               checkEmailAndSave(email, firstName, lastName, company);
               email = buildEmailString([company, firstName], separator, domain);

               // TODO check if email is valid
               // if valid save to csv file
               checkEmailAndSave(email, firstName, lastName, company);

               email = buildEmailString([firstName, company], separator, domain);

               // TODO check if email is valid
               // if valid save to csv file
               checkEmailAndSave(email, firstName, lastName, company);

            }
         }


         if (firstName && lastName && company) {

            email = buildEmailString([lastName, company, firstName], separator, domain);

            // TODO check if email is valid
            // if valid save to csv file
            checkEmailAndSave(email, firstName, lastName, company);

            email = buildEmailString([company, lastName, firstName], separator, domain);

            // TODO check if email is valid
            // if valid save to csv file
            checkEmailAndSave(email, firstName, lastName, company);

            email = buildEmailString([company, firstName, lastName], separator, domain);

            // TODO check if email is valid
            // if valid save to csv file
            checkEmailAndSave(email, firstName, lastName, company);

            email = buildEmailString([firstName, company, lastName], separator, domain);

            // TODO check if email is valid
            // if valid save to csv file
            checkEmailAndSave(email, firstName, lastName, company);

            email = buildEmailString([firstName, lastName, company], separator, domain);
            // TODO check if email is valid
            // if valid save to csv file

            checkEmailAndSave(email, firstName, lastName, company);

            email = buildEmailString([lastName, firstName, company], separator, domain);


            // TODO check if email is valid
            // if valid save to csv file
            checkEmailAndSave(email, firstName, lastName, company);

         }


      });
   });


}

// build Email String

function buildEmailString(parts, separator, domain) {

   if (config.debug) {
      console.log(parts.join(""));
   }
   parts = parts.map((part) => {

      return part.split(" ").join(separator);
   });

   const email = parts.join(separator) + '@' + domain;

   if (config.debug) {

      console.log(email);
   }
   return email;

}

// check email if vaild save it 

function checkEmailAndSave(email, firstName, lastName, company) {


   const record = { "Firstname": firstName, "LastName": lastName, "Company": company, "Email": email };

   setTimeout(function () {
      unirest.get('http://3.83.120.98:8080/Email_Checker/TrueMailServlet')
         .query({
            email: email
         })
         .end(function (response) {
            if (response.body == undefined) {

               console.log("erro");
            } else {
               //  console.log(response.body);

               myobject = JSON.parse(response.body);
               console.log(myobject);

               if (myobject.smtp_check == true) {

                  writer.write(record)
               } else {

                  console.log(response.raw_body);
               }
            }
         });
   }, 3000)

  
}



//console.log(config);
