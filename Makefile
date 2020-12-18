TOP=VCPU
SRC=CPU.v
BIN_DIR=obj_dir

default: all

clean:
	rm -r ${BIN_DIR}

compile-verilog:
	sbt "runMain cpu.main --target-dir obj_dir"

verilate: compile-verilog
	# Verilator creates cpp code out of verilog files
	verilator --cc obj_dir/${SRC}

	# Compiles generated cpp into library
	$(MAKE) -j -C ${BIN_DIR} -f ${TOP}.mk ${TOP}__ALL.a

build-cpu:
	# Compiles wrapper for simulator
	$(MAKE) -j -C ${BIN_DIR} -f ${TOP}.mk ../src/main/cpp/main.o verilated.o
	mv src/main/cpp/main.o ${BIN_DIR}
	mv src/main/cpp/main.d ${BIN_DIR}

	# Builds Simulation Application
	g++ ${BIN_DIR}/main.o \
		${BIN_DIR}/${TOP}__ALL.a \
		${BIN_DIR}/verilated.o \
		-o ${BIN_DIR}/${TOP}

all: verilate build-cpu