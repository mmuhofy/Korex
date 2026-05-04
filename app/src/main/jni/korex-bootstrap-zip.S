// Links the bootstrap zip file as binary data into the shared library.
// The zip file path is set by the build system via -DBOOTSTRAP_ZIP_PATH.
    .section .rodata
    .global _binary_bootstrap_zip_start
    .global _binary_bootstrap_zip_end
_binary_bootstrap_zip_start:
    .incbin BOOTSTRAP_ZIP_PATH
_binary_bootstrap_zip_end: