# From: poky/meta/recipes-devtools/qemu/qemu-system-native_3.1.0.bb
# Branch: warrior
# rev: 48522906a261f9a552f13b146aa7f3691be37002
BPN = "qemu"

require qemu-native.inc

# As some of the files installed by qemu-native and qemu-system-native 
# are the same, we depend on qemu-native to get the full installation set
# and avoid file clashes
DEPENDS = "glib-2.0-native zlib-native pixman-native qemu-native seabios-native"
RDEPENDS = "seabios-native"

EXTRA_OECONF_append = " --target-list=${@get_qemu_system_target_list(d)}"

PACKAGECONFIG ??= "fdt alsa kvm"

# Handle distros such as CentOS 5 32-bit that do not have kvm support
PACKAGECONFIG_remove = "${@'kvm' if not os.path.exists('/usr/include/linux/kvm.h') else ''}"

do_install_append() {
    install -Dm 0755 ${WORKDIR}/powerpc_rom.bin ${D}${datadir}/qemu

    # The following is also installed by qemu-native
    rm -f ${D}${datadir}/qemu/trace-events-all
    rm -rf ${D}${datadir}/qemu/keymaps
}
