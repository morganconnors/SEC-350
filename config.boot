firewall {
    name DMZ-to-LAN {
        default-action drop
        enable-default-log
        rule 1 {
            action accept
            state {
                established enable
            }
        }
        rule 10 {
            action accept
            description "Allow Wazuh agent communications with Wazuh server"
            destination {
                address 172.16.200.10
                port 1514,1515
            }
            protocol tcp
        }
    }
    name DMZ-to-WAN {
        default-action drop
        enable-default-log
        rule 1 {
            action accept
            state {
                established enable
            }
        }
        rule 999 {
            action accept
            disable
            source {
                address 172.16.50.4
            }
        }
    }
    name LAN-to-DMZ {
        default-action drop
        enable-default-log
        rule 1 {
            action accept
            state {
                established enable
            }
        }
        rule 20 {
            action accept
            description "Allow 80/tcp from LAN to Web01"
            destination {
                address 172.16.50.3
                port 80
            }
            protocol tcp
            source {
            }
        }
        rule 30 {
            action accept
            description "Allow 22/tcp from mgmt01 to the DMZ"
            destination {
                address 172.16.50.0/29
                port 22
            }
            protocol tcp
            source {
                address 172.16.150.10
            }
        }
        rule 40 {
            action accept
            description confusion
            destination {
                address 172.16.50.0/29
                port 22
            }
            protocol tcp
            source {
                address 172.16.200.10
            }
        }
        rule 999 {
            action accept
            destination {
                address 172.16.50.3
                port 22
            }
            disable
            protocol tcp
            source {
                address 172.16.200.10
            }
        }
    }
    name LAN-to-MGMT {
        default-action drop
        enable-default-log
    }
    name LAN-to-WAN {
        default-action drop
        enable-default-log
        rule 1 {
            action accept
        }
    }
    name WAN-to-DMZ {
        default-action drop
        enable-default-log
        rule 1 {
            action accept
            state {
                established enable
            }
        }
        rule 10 {
            action accept
            description "Allow HTTP from WAN to DMZ"
            destination {
                address 172.16.50.3
                port 80
            }
            protocol tcp
        }
        rule 20 {
            action accept
            destination {
                address 172.16.50.4
                port 22
            }
            protocol tcp
            source {
            }
        }
    }
    name WAN-to-LAN {
        default-action drop
        enable-default-log
        rule 1 {
            action accept
            state {
                established enable
            }
        }
    }
}
interfaces {
    ethernet eth0 {
        address 10.0.17.141/24
        description SEC-350-02-WAN
        hw-id 00:50:56:a1:ef:75
    }
    ethernet eth1 {
        address 172.16.50.2/29
        description SEC-350-02-DMZ
        hw-id 00:50:56:a1:51:56
    }
    ethernet eth2 {
        address 172.16.150.2/24
        description SEC-350-02-LAN
        hw-id 00:50:56:a1:a3:cd
    }
    loopback lo {
    }
}
nat {
    destination {
        rule 10 {
            description "HTTP --> Web01"
            destination {
                port 80
            }
            inbound-interface eth0
            protocol tcp
            translation {
                address 172.16.50.3
                port 80
            }
        }
        rule 20 {
            destination {
                port 22
            }
            inbound-interface eth0
            protocol tcp
            translation {
                address 172.16.50.4
                port 22
            }
        }
    }
    source {
        rule 10 {
            description "NAT from DMZ to WAN"
            outbound-interface eth0
            source {
                address 172.16.50.0/29
            }
            translation {
                address masquerade
            }
        }
        rule 20 {
            description "NAT from LAN to WAN"
            outbound-interface eth0
            source {
                address 172.16.150.0/24
            }
            translation {
                address masquerade
            }
        }
        rule 30 {
            description "NAT from MGMT to WAN"
            outbound-interface eth0
            source {
                address 172.16.200.0/28
            }
            translation {
                address masquerade
            }
        }
    }
}
protocols {
    rip {
        interface eth2 {
        }
        network 172.16.50.0/29
    }
    static {
        route 0.0.0.0/0 {
            next-hop 10.0.17.2 {
            }
        }
    }
}
service {
    dns {
        forwarding {
            allow-from 172.16.50.0/29
            allow-from 172.16.150.0/24
            listen-address 172.16.50.2
            listen-address 172.16.150.2
            system
        }
    }
    ssh {
        listen-address 0.0.0.0
    }
}
system {
    config-management {
        commit-revisions 100
    }
    conntrack {
        modules {
            ftp
            h323
            nfs
            pptp
            sip
            sqlnet
            tftp
        }
    }
    console {
        device ttyS0 {
            speed 115200
        }
    }
    host-name FW01-mconnors
    login {
        user vyos {
            authentication {
                encrypted-password $6$YUTCBnIl7XuxPfv7$UQXsMiDLSJsDs9mPJ2PQ.9IjjMks5MrKu6IlQRJsS.VIvkYeQXFvupJVrZMTQFYjkbTkRshVAYECJS337kHAS/
                plaintext-password ""
            }
        }
    }
    name-server 10.0.17.2
    ntp {
        server time1.vyos.net {
        }
        server time2.vyos.net {
        }
        server time3.vyos.net {
        }
    }
    syslog {
        global {
            facility all {
                level info
            }
            facility protocols {
                level debug
            }
        }
        host 172.16.50.5 {
            facility authpriv {
            }
        }
    }
}
zone-policy {
    zone DMZ {
        from LAN {
            firewall {
                name LAN-to-DMZ
            }
        }
        from WAN {
            firewall {
                name WAN-to-DMZ
            }
        }
        interface eth1
    }
    zone LAN {
        from DMZ {
            firewall {
                name DMZ-to-LAN
            }
        }
        from WAN {
            firewall {
                name WAN-to-LAN
            }
        }
        interface eth2
    }
    zone WAN {
        from DMZ {
            firewall {
                name DMZ-to-WAN
            }
        }
        from LAN {
            firewall {
                name LAN-to-WAN
            }
        }
        interface eth0
    }
}


// Warning: Do not remove the following line.
// vyos-config-version: "bgp@3:broadcast-relay@1:cluster@1:config-management@1:conntrack@3:conntrack-sync@2:dhcp-relay@2:dhcp-server@6:dhcpv6-server@1:dns-forwarding@3:firewall@7:flow-accounting@1:https@3:interfaces@26:ipoe-server@1:ipsec@9:isis@1:l2tp@4:lldp@1:mdns@1:monitoring@1:nat@5:nat66@1:ntp@1:openconnect@2:ospf@1:policy@3:pppoe-server@5:pptp@2:qos@1:quagga@10:rpki@1:salt@1:snmp@2:ssh@2:sstp@4:system@25:vrf@3:vrrp@3:vyos-accel-ppp@2:wanloadbalance@3:webproxy@2"
// Release version: 1.4-rolling-202209130217
