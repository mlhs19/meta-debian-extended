# base recipe: meta/recipes-support/libgpg-error/libgpg-error_1.35.bb
# base branch: warrior
# base commit: 775d89d45d55defe168c1ce3752de6ac3b7c7e61

SUMMARY = "Small library that defines common error values for all GnuPG components"
HOMEPAGE = "http://www.gnupg.org/related_software/libgpg-error/"
BUGTRACKER = "https://bugs.g10code.com/gnupg/index"

LICENSE = "GPLv2+ & LGPLv2.1+"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552 \
					file://COPYING.LIB;md5=2d5025d4aa3495befef8f17206a5b0a1 \
					file://src/gpg-error.h.in;beginline=2;endline=18;md5=cd91e3ad1265a0c268efad541a39345e \
					file://src/init.c;beginline=2;endline=17;md5=f01cdfcf747af5380590cfd9bbfeaaf7"

SECTION = "libs"

inherit debian-package
require recipes-debian/sources/libgpg-error.inc

FILESPATH_append = ":${COREBASE}/meta/recipes-support/libgpg-error/libgpg-error:"
SRC_URI += " file://pkgconfig.patch"

BINCONFIG = "${bindir}/gpg-error-config"

inherit autotools binconfig-disabled pkgconfig gettext multilib_header multilib_script

MULTILIB_SCRIPTS = "${PN}-dev:${bindir}/gpgrt-config"

CPPFLAGS += "-P"
do_compile_prepend() {
	TARGET_FILE=linux-gnu
	if [ ${TARGET_OS} = "mingw32" ]; then
		# There are no arch specific syscfg files for mingw32
		TARGET_FILE=
	elif [ ${TARGET_ARCH} = "arc" ]; then
		# ARC syscfg file is automatically aliased to i686-pc-linux-gnu
		TARGET_FILE=
	elif [ ${TARGET_OS} != "linux" ]; then
		TARGET_FILE=${TARGET_OS}
	fi

	case ${TARGET_ARCH} in
	  aarch64_be) TUPLE=aarch64-unknown-linux-gnu ;;
	  arm)	      TUPLE=arm-unknown-linux-gnueabi ;;
	  armeb)      TUPLE=arm-unknown-linux-gnueabi ;;
	  i586|i686)  TUPLE=i686-unknown-linux-gnu;;
	  mips64*)    TUPLE=mips64el-unknown-linux-gnuabi64 ;;
	  mips*el)    TUPLE=mipsel-unknown-linux-gnu ;;
	  mips*)      TUPLE=mips-unknown-linux-gnu ;;
	  x86_64)     TUPLE=x86_64-unknown-linux-gnu ;;
	  ppc)        TUPLE=powerpc-unknown-linux-gnu ;;
	  ppc64)      TUPLE=powerpc64-unknown-linux-gnu ;;
	  ppc64le)    TUPLE=powerpc64le-unknown-linux-gnu ;;
	  *)          TUPLE=${TARGET_ARCH}-unknown-linux-gnu ;;
	esac

	if [ -n "$TARGET_FILE" ]; then
		cp ${S}/src/syscfg/lock-obj-pub.$TUPLE.h \
			${S}/src/syscfg/lock-obj-pub.$TARGET_FILE.h
	fi
}

do_install_append() {
	# we don't have common lisp in OE
	rm -rf "${D}${datadir}/common-lisp/"
	oe_multilib_header gpg-error.h gpgrt.h
}

FILES_${PN}-dev += "${bindir}/gpg-error"
FILES_${PN}-doc += "${datadir}/libgpg-error/errorref.txt"

BBCLASSEXTEND = "native nativesdk"

SRC_URI += " file://run-ptest"
inherit ptest
RDEPENDS_${PN}-ptest += "make"

do_install_ptest() {
	t=${D}${PTEST_PATH}
	cp ${WORKDIR}/build/tests/Makefile $t
	cp ${WORKDIR}/build/tests/t-argparse $t
	cp ${WORKDIR}/build/tests/t-b64 $t
	cp ${WORKDIR}/build/tests/t-lock $t
	cp ${WORKDIR}/build/tests/t-logging $t
	cp ${WORKDIR}/build/tests/t-poll $t
	cp ${WORKDIR}/build/tests/t-printf $t
	cp ${WORKDIR}/build/tests/t-strerror $t
	cp ${WORKDIR}/build/tests/t-syserror $t
	cp ${WORKDIR}/build/tests/t-version $t
}
