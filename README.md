This program standardizes the memory, serial number and mac address field of different TDX records.

In order for this program to work on the production set of TDX records, you must change the link
in the GetRequest function and the PushRequest function from:
   "https://teamdynamix.umich.edu/SBTDWebApi/api/48/assets/" + assetID
to "https://teamdynamix.umich.edu/TDWebApi/api/48/assets/" + assetID
where the change is removing the SB from the WebApi section of the url.

This programs requires two files:
A well formatted input file called Input.txt and an output called Output.txt.

Input.txt (Well formatted input file):
In order for the file to be well formatted the first line must be the bearer token for the API.
All following lines should contain a singular assetID, so each line would be a singular assetID
for the targeted assets to update. 

Bearer Token:
To grab the bearer token needed for correct authorization of account in the TDX API, you first need to 
visit this link: https://teamdynamix.umich.edu/sbtdwebapi/api/auth/loginsso . Then login and copy and paste
the token provided to the first line in the Input.txt file.

Output.txt (Ouput File):
This file will store the assetID, inital values for target fields, and the updated values for those fields.
The first line will print out the order in which these targets are printed for the rest of the file.
If a field is blank in the asset, it will be printed as a singular space, meaning there will be an extra space 
in spots where the target field is empty.

Serial Number Standardization:
Standardizing the serial number will result in a serial number with all letters capitalized.

Memory Standardization:
Standardizing the memory will result in the input value (int of the memory) followed immediately by
the unit in which the memory is in (MB, GB or TB). If there is no unit following the input value, then
just the input value would be printed. When the unit does not match any of the three most common units, 
(MB, GB or TB) the result will be the initial value fed into the program.

Memory Examples:
12 mb -> 12MB  |
12 Gigs additional -> 12GB additional |
8.53 -> 8.53 

MAC Standardization:
Standardizing the MAC address will result in a MAC address with all letters capitalized and no dividing 
characters within a singular MAC address. In the case of there being more than one MAC address in the field,
the result would be the two addresses standardized separated by a singular semicolon. Any notes left in the field
would result in the field being returned unchanged. NOTE: If the field contains notes that are evenly divisible by 12
then the input would be converted into a MAC address standardized, with the nearby characters (asset 60404).

MAC Examples:
a8:60:b6:04:e5:0f -> A860B604E50F |
f4:39:09:0a:92:85; a0:8c:fd:d5:3d:ab -> F439090A9285;A08CFDD53DAB |
a0:d3:c1:2c:60:94 (lan) -> a0:d3:c1:2c:60:94 (lan)

ERROR Example:
a8:60:b6:04:e4:0f twelveletter -> A860B604E40F;TWELVELETTER