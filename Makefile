.SUFFIXES: .java .class

VDIR      = com/vitriol

CLASS     = RoleService
JAR       = proxy.jar
CLASSPATH = .

CLASSES   = \
			$(VDIR)/Props.class \
			RoleThread.class \
			RoleService.class
		 
TARGETS   = $(JAR) run

export CLASSPATH

all: $(TARGETS)

.java.class:
	javac $<

$(JAR): $(CLASSES)
	jar cf $@ $(CLASSES)

run: Makefile
	@echo building $@
	@echo \#!/bin/sh > $@
	@echo >> $@
	@echo CLASSPATH=$(CLASSPATH) >> $@
	@echo CLASS=$(CLASS) >> $@
	@echo >> $@
	@echo export CLASSPATH >> $@
	@echo >> $@
	@echo java \$$CLASS >> $@
	chmod +x $@

clean:
	$(RM) $(CLASSES)

clobber: clean
	$(RM) $(TARGETS)
