# PREFIX=$(HOME)/.keychain/
# USE YOUR PATH TO KEYCHAIN
PREFIX=/mnt/c/Users/philip.buhler/.keychain/

KEYCHAIN_OBJ_DIR=$(PREFIX)/obj/keychain-java
KEYCHAIN_LIB_DIR=$(PREFIX)/lib/java
KEYCHAIN_BIN_DIR=$(PREFIX)/bin
KEYCHAIN_INCLUDE_DIR=$(PREFIX)/include
KEYCHAIN_INSTALL_DIR=$(PREFIX)

JAVA_TESTS=\
../../src/io/keychain/mitm/KeychainClient.java \
../../src/io/keychain/mitm/StandardClient.java \
../../src/io/keychain/mitm/Client.java \
../../src/io/keychain/mitm/util/DirectoryThread.java \
../../src/io/keychain/mitm/util/PairHelper.java

JAVA_TEST_CLASSES=$(JAVA_TESTS:.java=.class)

all: $(JAVA_TEST_CLASSES)

$(JAVA_TEST_CLASSES): $(JAVA_TESTS)
	javac -classpath $(KEYCHAIN_LIB_DIR)/keychain.jar:../../lib/json-20201115.jar -sourcepath $(<D) \
-d $(<D) $<

clean:
	rm -f $(JAVA_TEST_CLASSES)

