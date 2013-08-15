DNS53 Server Installation

	This webservice serves as a DNS, which can be queried to look up IP address of the target
resource record set. DNS53 also allows the user to create / delete hosted zones and create /
delete resource record sets. These resource record sets values are not checked (just as AWS
Route 53) does not check these values. If they are wrong, your hosted zone will just not work
due to erroneous resource record sets. This also delays the DNS server to restart, which occur
every one minute. For this reason, it is recommended that the user sets up a slave DNS to
point at this DNS53 as its master. This setup allows the DNS53 to take little bit longer to 
restart without any penalty. Without the slave DNS, all domain name queries will be unavailable
or halt during the restarting phase. In such an environment, user must make sure to pass the
right value at all times to make sure that DNS can restart quickly (but not recommended since
everyone makes mistakes). Therefore, it is HIGHLY recommended to set up a slave DNS.

1. Install DNS53Server; this should be installed along with other Transcend Computing services.
2. Set up a slave DNS using Bind, dnsmasq, or any DNS of your choice. Point to DNS53Server as
	its master DNS.
3. For the network that wants to use DNS53Server, point at the slave DNS as its name server.