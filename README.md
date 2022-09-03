
# Dice Plugin
A fork of Highfire1's Ultimate Dice Plugin that adds multiple new features:
 - Support for dice pools
 - Option to convert "raw" rolls into pool rolls with a certain criteria (10d20 -> 10d20@11)
 - Option to move individual roll results to hovertext
 - Ranged message support (only sending to players within a certain range) 
 - PlaceholderAPI integration (for nicknames as well as chat range) 
 - Ability to change what gets rolled when only /roll is called 

Example formats:
- /roll 1d20
- /roll 1d20+5
- /r 10d2+5d3*2d9

To roll a dice pool:
- /roll 10d20@10 

Ranged mode (can be set as default in the config):
- /roll 10d20@10 -range

Screenshots of various configurations:

![image](https://user-images.githubusercontent.com/37964537/188258586-309d5a0e-4856-48af-92dd-795315cf2174.png)

![image](https://user-images.githubusercontent.com/37964537/188258850-2f2beddf-5eba-480f-8cc1-db4dffd53882.png)

![image](https://user-images.githubusercontent.com/37964537/188258833-b651dc07-c43c-4407-8145-63cfe00d8f8a.png)
