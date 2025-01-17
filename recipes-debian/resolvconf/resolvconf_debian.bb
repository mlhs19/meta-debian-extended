#
# base recipe: meta/recipes-connectivity/resolvconf/resolvconf_1.79.bb
# base branch: warrior
# base commit: 4b777079066e6fa72d7c9db1e1cae3ebb9338351
#

SUMMARY = "name server information handler"
DESCRIPTION = "Resolvconf is a framework for keeping track of the system's \
information about currently available nameservers. It sets \
itself up as the intermediary between programs that supply \
nameserver information and programs that need nameserver \
information."
SECTION = "console/network"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=c93c0550bd3173f4504b2cbd8991e50b"
AUTHOR = "Thomas Hood"
HOMEPAGE = "http://packages.debian.org/resolvconf"
RDEPENDS_${PN} = "bash"

inherit debian-package
require recipes-debian/sources/${BPN}.inc

FILESPATH_append = ":${COREBASE}/meta/recipes-connectivity/${BPN}/${BPN}:"
SRC_URI += "file://fix-path-for-busybox.patch \
           file://99_resolvconf \
          "

inherit allarch

do_compile () {
	:
}

do_install () {
	install -d ${D}${sysconfdir}/default/volatiles
	install -m 0644 ${WORKDIR}/99_resolvconf ${D}${sysconfdir}/default/volatiles
	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		install -d ${D}${sysconfdir}/tmpfiles.d
		echo "d /run/${BPN}/interface - - - -" \
		     > ${D}${sysconfdir}/tmpfiles.d/resolvconf.conf
	fi
	install -d ${D}${base_libdir}/${BPN}
	install -d ${D}${sysconfdir}/${BPN}
	ln -snf ${localstatedir}/run/${BPN} ${D}${sysconfdir}/${BPN}/run
	install -d ${D}${sysconfdir} ${D}${base_sbindir}
	install -d ${D}${mandir}/man8 ${D}${docdir}/${P}
	cp -pPR etc/* ${D}${sysconfdir}/
	chown -R root:root ${D}${sysconfdir}/
	install -m 0755 bin/resolvconf ${D}${base_sbindir}/
	install -m 0755 bin/list-records ${D}${base_libdir}/${BPN}
	install -d ${D}/${sysconfdir}/network/if-up.d
	install -m 0755 debian/resolvconf.000resolvconf.if-up ${D}/${sysconfdir}/network/if-up.d/000resolvconf
	install -d ${D}/${sysconfdir}/network/if-down.d
	install -m 0755 debian/resolvconf.resolvconf.if-down ${D}/${sysconfdir}/network/if-down.d/resolvconf
	install -m 0644 README ${D}${docdir}/${P}/
	install -m 0644 man/resolvconf.8 ${D}${mandir}/man8/
}

pkg_postinst_${PN} () {
	if [ -z "$D" ]; then
		if command -v systemd-tmpfiles >/dev/null; then
			systemd-tmpfiles --create ${sysconfdir}/tmpfiles.d/resolvconf.conf
		elif [ -e ${sysconfdir}/init.d/populate-volatile.sh ]; then
			${sysconfdir}/init.d/populate-volatile.sh update
		fi
	fi
}

FILES_${PN} += "${base_libdir}/${BPN}"
