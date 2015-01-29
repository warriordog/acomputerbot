AcomputerBot - A simple java IRC bot
============

AcomputerBot is a simple IRC bot written in java.  The only external dependancy is [CompCore](https://github.com/warriordog/CompCore), although it includes a modified version of [sIRC](https://github.com/sorcix/sIRC) to handle the IRC connection.  Java 8 is also required.
Although unfinished, AcomputerBot is fully configurable and is easily expanded to add additional commands.  Some features are listed here:

* NickServ-authenticated Admin list + configurable password (admins must log in, and if they log out of IRC their session is invalidated)
* Dozens of commands, and hundreds of aliases.
* Easily expandable command framework
* Can use registered nicks by giving entering NickServ password in config
* Adjustable activation character
* Configurable rate limits and abuse protection
* Can join multiple channels at once and can work via both PMs and channel chat
* All commands logged to console and files
* BlackList/WhiteList support
* Responds to "*BOTINFO" querys
* Channel ops can remove bot to prevent abuse
* Plugin support
* All built-in commands are removable plugins
