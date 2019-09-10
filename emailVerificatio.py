import re
import smtplib
import dns.resolver
from flask import Flask,jsonify,request

# {"score":0.96,"format_valid":true,"role":false,
# "domain":"bizlem.com","mx_found":true,"did_you_mean":"",
# "smtp_check":true,"catch_all":null,"free":false,
# "user":"mohit.raj","email":"mohit.raj@bizlem.com","disposable":false}


# Address used for SMTP MAIL FROM command
fromAddress = 'corn@bt.com'

# Simple Regex for syntax checking
regex = '^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,})$'
app=Flask(__name__)
@app.route("/Email_Checker/TrueMailServlet/",methods=["GET"])
def verify_email():
        domain=""
        format_valid=True
        mxRecord=True
        smtp_check=True
        email=""
        return_json={"score":0.96,"format_valid":format_valid,"role":False,"domain":domain,"mx_found":mxRecord,"did_you_mean":"","smtp_check":smtp_check,
                                "catch_all":"null","free":False,"user":"mohit.raj","email":email,"disposable":False}

        # Email address to verify
        # inputAddress = input('Please enter the emailAddress to verify:')
        email=request.args.get('email')
        print('enterd email----',email)
        addressToVerify = str(email)

        # Syntax check
        match = re.match(regex, addressToVerify)
        if match == None:
                print('Bad Syntax')
                # raise ValueError('Bad Syntax')
                format_valid=False
                smtp_check=False
                return_json.update({"format_valid":format_valid,"email":email,"smtp_check":smtp_check})
                return jsonify(return_json)

        # Get domain for DNS lookup
        splitAddress = addressToVerify.split('@')
        domain = str(splitAddress[1])
        print('Domain:', domain)

        # MX record lookup
        try:
                records = dns.resolver.query(domain, 'MX')
                mxRecord = records[0].exchange
                mxRecord = str(mxRecord)
        except Exception as ee:
                print('MX record lookup error----',ee)
                # return jsonify()

        # SMTP lib setup (use debug level for full output)
        server = smtplib.SMTP()
        server.set_debuglevel(0)

        # SMTP Conversation
        code=""
        try:

                server.connect(mxRecord)
                server.helo(server.local_hostname) ### server.local_hostname(Get local server hostname)
                server.mail(fromAddress)
                code, message = server.rcpt(str(addressToVerify))
                server.quit()
        except Exception as e:
                print ('errot---',e)
        #print(code)
        #print(message)

        # Assume SMTP response 250 is success
        if code == 250:
                print('Success')
                return_json.update({"format_valid":format_valid,"domain":domain,"mx_found":mxRecord,"smtp_check":smtp_check,"email":email})
                return jsonify(return_json)
        else:
                print('Bad')
                smtp_check=False
                return_json.update({"format_valid":format_valid,"domain":domain,"mx_found":mxRecord,"smtp_check":smtp_check,"email":email})
                return jsonify(return_json)

if __name__=="__main__":
        app.run('0.0.0.0',debug=True,port=8080)
