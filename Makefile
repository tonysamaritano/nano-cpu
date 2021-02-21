TOP=VCFU
SRC=CFU.v
SRC_DIR=src/main/cpp
BIN_DIR=build
JAVA_DIR=target

default: all

clean:
	# Removes build dir and whatever the 'target' directory that
	# scala makes. I don't know what that directory is or why I
	# can't control where it is put
	rm -r ${BIN_DIR} ${JAVA_DIR}

	# Scala also builds folders in the project folder that I don't
	# understand. There are other files in the project folder that
	# seem important though
	rm -r project/project
	rm -r project/target

compile-verilog:
	mkdir -p ${BIN_DIR}
	sbt "runMain cfu.main --target-dir ${BIN_DIR}"

verilate:
	# Verilator creates cpp code out of verilog files
	verilator \
		--cc ${BIN_DIR}/${SRC} \
		--exe ${SRC_DIR}/main.cpp \
		--trace \
		--Mdir ${BIN_DIR} \
		-CFLAGS -g \
		-CFLAGS -I../${SRC_DIR}/include

	# Compiles generated cpp into library
	$(MAKE) -j -C ${BIN_DIR} -f ${TOP}.mk ${TOP}__ALL.a

build-cpu:
	# Compiles wrapper for simulator
	$(MAKE) -j -C ${BIN_DIR} -f ${TOP}.mk \
		main.o \
		verilated.o \
		verilated_vcd_c.o

	# g++ -g -fpermissive -Isrc/main/cpp/include -o ${BIN_DIR}/module.a src/main/cpp/src/Module.cpp

	# Builds Simulation Application
	g++ -fsanitize=address -static-libasan \
		-g ${BIN_DIR}/main.o \
		${BIN_DIR}/${TOP}__ALL.a \
		${BIN_DIR}/verilated.o \
		${BIN_DIR}/verilated_vcd_c.o \
		-o ${BIN_DIR}/${TOP}

all: compile-verilog verilate build-cpu

.PHONY: examples/subroutine

# Programs
examples/subroutine:
	mkdir -p ${BIN_DIR}
	python3 tools/compiler.py \
		--asm examples/subroutine/math.asm \
		--asm examples/subroutine/subroutine.asm \
		--bin ${BIN_DIR}/subroutine.bin