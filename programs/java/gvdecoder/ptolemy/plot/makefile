# Makefile for Java ptplot program
#
# @Authors: Christopher Hylands
#
# @Version: $Id: makefile,v 1.170.2.11 2001/03/20 16:31:37 cxh Exp $
#
# @Copyright (c) 1997-2001 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		ptolemy/plot

DIRS = 		demo doc test plotml compat

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	plot
# If you change PTVERSION, modify the version numbers in:
#    PlotBox.java,
#    doc/main.htm, doc/changes.htm, doc/install.htm
PTVERSION =	5.1p1
PTDIST =	ptplot$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		plotml/plotml.jar \
		compat/compat.jar

PTCLASSALLJAR = plot.jar

# Jar file consisting of classes used by an applet.
PTAUXJAR = 		plotapplet.jar

# Jar file consisting of classes used by pxgraph applets.
# The pxgraph classes appear in this jar file and in the plot.jar
PXGRAPHAPPLETJAR =	pxgraphapplet.jar

# Jar file consisting of classes used by an application, including the
# com/microstar/xml classes.  Note that this jar file does not include
# the pxgraph classes.
PLOTMLAPPLETJAR = plotmlapplet.jar

# Jar file consisting of classes used by an application, including the
# ptolemy/gui and com/microstar/xml classes.  Note that this
# jar file does not include the pxgraph classes.
PLOTAPPLICATIONJAR = plotapplication.jar

# Java Sources, keep these alphabetical,
JSRCS = \
	CmdLineArgException.java \
	EditablePlot.java \
	EditListener.java \
	EPSGraphics.java \
	Histogram.java \
	HistogramApplet.java \
	Plot.java \
	PlotApplet.java \
	PlotApplication.java \
	PlotBox.java \
	PlotDataException.java \
	PlotFormatter.java \
	PlotFrame.java \
	PlotLive.java \
	PlotLiveApplet.java \
	PlotPoint.java

# Used to build distributions with Installshield's Multiplatform Installer
INSTALLSHIELD_PROJECT = ptplot_ismp.xml

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) \
	$(INSTALLSHIELD_PROJECT) \
	copyright.txt \
	index.htm \

# Non-source files such as README files and subdirectories
MISC_FILES = test img #bean

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	adm \
	histogram \
	manifest.tmp \
	$(PTCLASSALLJAR) \
	unix$(INSTALLSHIELD_PROJECT) \
	'EditablePlot$$EditMouseListener.class' \
	'EditablePlot$$ModifyListener.class' \
	'EditablePlot$$UndoListener.class' \
	'Histogram$$1.class' \
	'Histogram$$2.class' \
	'Histogram$$3.class' \
	'Plot$$1.class' \
	'Plot$$2.class' \
	'Plot$$3.class' \
	'Plot$$4.class' \
	'Plot$$5.class' \
	'Plot$$6.class' \
	'Plot$$7.class' \
	ptolemy.plot$(JVERSION).tar.gz \
	ptolemy.plot$(JVERSION).zip \
	$(PTAUXJAR) \
	$(PXGRAPHAPPLETJAR) \
	$(PLOTMLAPPLETJAR) \
	$(PLOTAPPLICATIONJAR) \
	'PlotBox$$1.class' \
	'PlotBox$$ButtonListener.class' \
	'PlotBox$$CommandListener.class' \
	'PlotBox$$DragListener.class' \
	'PlotBox$$ZoomListener.class' \
	'PlotFormatter$$1.class' \
	'PlotFormatter$$2.class' \
	'PlotLive$$StartButtonListener.class' \
	'PlotLive$$StopButtonListener.class' \
	'Plot$$Format.class' \
	'PlotApplication$$1.class' \
	'PlotFrame$$FileMenuListener.class' \
	'PlotFrame$$FormatListener.class' \
	'PlotFrame$$SpecialMenuListener.class'

JCLASS = $(JSRCS:%.java=%.class)

all: sources jclass

install: all jars \
		$(PXGRAPHAPPLETJAR) $(PLOTMLAPPLETJAR) $(PLOTAPPLICATIONJAR)
	(cd ../../bin; $(MAKE) install)

# Class files used in all applets and applications
BASECLASSFILES = \
		$(ME)/CmdLineArgException.class \
		$(ME)/EPSGraphics.class \
		$(ME)/Histogram.class \
		'$(ME)/Histogram$$1.class' \
		'$(ME)/Histogram$$2.class' \
		'$(ME)/Histogram$$3.class' \
		$(ME)/HistogramApplet.class \
		$(ME)/Plot.class \
		'$(ME)/Plot$$1.class' \
		'$(ME)/Plot$$2.class' \
		'$(ME)/Plot$$3.class' \
		'$(ME)/Plot$$4.class' \
		'$(ME)/Plot$$5.class' \
		'$(ME)/Plot$$6.class' \
		'$(ME)/Plot$$7.class' \
		$(ME)/PlotBox.class \
		'$(ME)/PlotBox$$1.class' \
		'$(ME)/PlotBox$$ButtonListener.class' \
		$(ME)/PlotDataException.class \
		$(ME)/PlotPoint.class \
		'$(ME)/Plot$$Format.class' \
		'$(ME)/PlotFormatter$$1.class' \
		'$(ME)/PlotFormatter$$2.class' \
		'$(ME)/PlotFormatter.class' \
		'$(ME)/PlotBox$$CommandListener.class' \
		'$(ME)/PlotBox$$DragListener.class' \
		'$(ME)/PlotBox$$ZoomListener.class' \
		$(ME)/img/fill.gif \
		$(ME)/img/format.gif \
		$(ME)/img/print.gif \
		$(ME)/img/reset.gif

# Class files used in simple applets
APPLETCLASSFILES = \
		$(ME)/PlotApplet.class \
		$(ME)/PlotLive.class \
		$(ME)/PlotLiveApplet.class

# Class files that permit editing and histograms, used by
# complex applets and applications
COMPLEXCLASSFILES = \
		'$(ME)/EditablePlot$$EditMouseListener.class' \
		'$(ME)/EditablePlot$$ModifyListener.class' \
		'$(ME)/EditablePlot$$UndoListener.class' \
		$(ME)/EditablePlot.class \
		$(ME)/EditListener.class \
		$(ME)/Histogram.class \
		$(ME)/HistogramApplet.class \
		'$(ME)/PlotLive$$StartButtonListener.class' \
		'$(ME)/PlotLive$$StopButtonListener.class' \
		'$(ME)/PlotFrame$$FileMenuListener.class' \
		'$(ME)/PlotFrame$$FormatListener.class' \
		'$(ME)/PlotFrame$$SpecialMenuListener.class' \
		$(ME)/PlotFrame.class

# Class files used in applications
APPLICATIONCLASSFILES = \
		$(COMPLEXCLASSFILES) \
		'$(ME)/PlotApplication$$1.class' \
		$(ME)/PlotApplication.class

# Jar file for simple applets
$(PTAUXJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); $(JAR) cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLETCLASSFILES) \
            )


# Class files used in pxgraph applets
$(PXGRAPHAPPLETJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); $(JAR) cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLETCLASSFILES) \
		$(ME)/compat/PxgraphApplet.class \
		$(ME)/compat/PxgraphApplication.class \
		$(ME)/compat/PxgraphParser.class \
            )

# Jar file for PlotML applets, including xml classes
$(PLOTMLAPPLETJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); $(JAR) cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLETCLASSFILES) \
		$(COMPLEXCLASSFILES) \
		$(ME)/plotml/*.class \
		com/microstar/xml/*.class \
            )

# Jar file for applications, including ptolemy/gui and xml classes
$(PLOTAPPLICATIONJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); $(JAR) cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLICATIONCLASSFILES) \
		$(COMPLEXCLASSFILES) \
		ptolemy/gui/*.class \
		$(ME)/plotml/*.class \
		com/microstar/xml/*.class \
            )

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

# Include rules necessary to build a distribution
include $(ROOT)/mk/ptdist.mk

PTTMPME= $(PTTMPDIR)/$(PTDIST)/$(ME)
# Fix the temporary directory used when building a distribution
fixtmpdist:
	@echo "Fixing $(PTTMPDIR)/$(PTDIST)"
	echo "all makefiles sources install docs depend checkjunk sccsinfo sccsclean test extraclean clean: mk/ptII.mk" > $(PTTMPDIR)/$(PTDIST)/makefile
	echo '	cd ptolemy/plot; make $$@' >>$(PTTMPDIR)/$(PTDIST)/makefile
	echo '# Generate ptII.mk by running configure' >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo 'mk/ptII.mk: configure mk/ptII.mk.in' >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo '	./configure' >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "realclean itcldocs tests idocs weblint dists:" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "	echo \"Nothing need be done for $$@ in ptolemy/plot)\"" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "$(PTDIST) - A Java plotting package" > \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Ptplot is a set of two dimensional signal plotters" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "implemented in Java." >>  $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Ptplot is embeddable in applets and applications." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "For more information, see ptolemy/plot/doc/index.htm" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Installer notes " >> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "If you are using the auto installer, then note that" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "under Windows, the Ptplot programs will be added to" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "your start menu under Ptolemy -> Ptplot." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	#mv /tmp/ptplotfix $(PTTMPME)/makefile
	@echo "JShield needs the ico file to be in the top level directory"
	cp $(PTTMPME)/doc/img/ptplot.ico \
		$(PTTMPDIR)/$(PTDIST)
	@echo "Copy over xml stuff"
	mkdir -p $(PTTMPDIR)/$(PTDIST)/com
	cp -r "$(PTII)/com/microstar" "$(PTII)/com/makefile" \
		$(PTTMPDIR)/$(PTDIST)/com
	@echo "Copy over bin"
	mkdir -p $(PTTMPDIR)/$(PTDIST)/bin
	cp -r   "$(PTII)/bin/ptinvoke.in" \
		"$(PTII)/bin/histogram.bat" \
		"$(PTII)/bin/ptplot.bat" \
		"$(PTII)/bin/pxgraph.bat" \
		$(PTTMPDIR)/$(PTDIST)/bin
	@echo "Create bin/makefile"
	echo "ME =		bin" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "ROOT =		.." >> 	$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "CONFIG =	$(ROOT)/mk/ptII.mk" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "include $(CONFIG)" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "SHELL_FILES = \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	histogram \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ptplot \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	pxgraph" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "# DOS batch files" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "BAT_FILES = \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	histogram.bat \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ptplot.bat \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	pxgraph.bat " >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	EXTRA_SRCS = \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ptinvoke.in \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	$(BAT_FILES)" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "all: ptinvoke" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	-chmod a+x ptinvoke" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	rm -f histogram" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ln -s ptinvoke histogram" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	rm -f ptplot" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ln -s ptinvoke ptplot" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	rm -f pxgraph" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ln -s ptinvoke pxgraph" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	install: all" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	-chmod a+x $(SHELL_FILES) $(BAT_FILES)" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "ptinvoke: ptinvoke.in ../config.status" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	(cd ..; ./config.status)" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "# zip might not get the permissions of configure right, so we run /bin/sh" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "../config.status: ../configure" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	(cd ..; /bin/sh ./configure)" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "# Get the rest of the rules" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "include $(ROOT)/mk/ptcommon.mk" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	@echo "Copy over gui stuff"
	cp -r "$(PTII)/ptolemy/gui" $(PTTMPDIR)/$(PTDIST)/ptolemy
	#
	# Setting up top level doc directory
	#
	mkdir -p $(PTTMPDIR)/$(PTDIST)/doc/img
	cp -r "$(PTII)/doc/banner.htm" \
		"$(PTII)/doc/contentsheader.htm" \
		"$(PTII)/doc/default.css" \
		"$(PTII)/doc/footer.htm" \
		"$(PTII)/doc/mainheader.htm" \
		"$(PTII)/doc/toc.css" \
		 $(PTTMPDIR)/$(PTDIST)/doc
	cp -r "$(PTII)/doc/img/ptIIplanetIcon.gif" \
		"$(PTII)/doc/img/ptIIbanner3.gif" \
		"$(PTII)/doc/img/prev.gif" \
		$(PTTMPDIR)/$(PTDIST)/doc/img
	# Create a file in the top level doc directory that points to
	# the real docs
	echo "<html>" > $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "<head>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "<title>Ptplot</title>" > $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "</head>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "<h1>Ptplot</h1>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo 'The Ptplot documentation is in <a href="../ptolemy/plot/doc/index.htm"><code>../ptolemy/plot/doc</code>' >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "</body>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "</htmp>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	#
	# Setting up doc/codeDoc directory
	#
	mkdir -p $(PTTMPDIR)/$(PTDIST)/doc/codeDoc
	cd $(PTTMPDIR)/$(PTDIST); \
	CLASSPATH=. $(JAVADOC) $(JDOCFLAGS) -d doc/codeDoc \
		com.microstar.xml \
		ptolemy.gui \
		ptolemy.plot \
		ptolemy.plot.compat \
		ptolemy.plot.plotml
	# Remove files that we don't want to include
	# GNU Tar under cygwin has problems excluding files
	rm -rf $(PTTMPME)/adm $(PTTMPME)/*.gz $(PTTMPME)/*.zip \
		$(PTTMPME)/Projects
	-find $(PTTMPME) -name CVS -exec rm -rf {} \;
	@echo "Fixing up doc/download directory"
	rm -rf $(PTTMPME)/doc/download
	mkdir $(PTTMPME)/doc/download
	echo "<html>" > $(PTTMPME)/doc/download/index.htm
	echo "<head>" >> $(PTTMPME)/doc/download/index.htm
	echo "<title>Download Ptplot</title>" >> $(PTTMPME)/doc/download/index.htm
	echo '<body bgcolor="#faf0e6">'  >> $(PTTMPME)/doc/download/index.htm
	echo "<H1>Download Ptplot</H1>" >> $(PTTMPME)/doc/download/index.htm
	echo "The Ptplot distribution itself does not include all the available download formats." >> $(PTTMPME)/doc/download/index.htm
	echo 'See <a href="http://ptolemy.eecs.berkeley.edu/java/ptplot" target="_top"><code>http://ptolemy.eecs.berkeley.edu/java/ptplot</code></a>' >> $(PTTMPME)/doc/download/index.htm
	echo "</body>" >> $(PTTMPME)/doc/download/index.htm
	echo "</html>" >> $(PTTMPME)/doc/download/index.htm
	-chmod -R a+rX $(PTTMPME)

ISPMP_PROJECT_DIR= [cC]:\\users\\cxh\\ptII\\adm\\trees\\ptII1.0beta\\ptolemy\\plot
ISPMP_WINDOWS_SRC= $(ISPMP_PROJECT_DIR)\\adm\\tmp\\$(PTDIST)
# Run this on doppler with:
# rm unixptplot_ismp.xml; make build_ispmp

WEBSITE=/vol/ptolemy/pt0/ptweb/java/$(PTDIST)
update_website:
	rm -rf $(WEBSITE)
	cat $(PTDIST).tar.gz | (cd /vol/ptolemy/pt0/ptweb/java; \
		$(GNUTAR) -zxf -) 
	-cp doc/download/* $(WEBSITE)/ptolemy/plot/doc/download/
	cp Projects/adm/installshield/ptplot5_1setup.jar \
		Projects/adm/installshield/ptplot5_1setup.exe \
		Projects/adm/installshield/applet/ms.cab \
		Projects/adm/installshield/applet/ns.jar \
		$(WEBSITE)/ptolemy/plot/doc/download/
	gzip $(WEBSITE)/ptolemy/plot/doc/download/ptplot5_1setup.jar
	cp \
	        $(PTDIST).tar.gz \
	        $(PTDIST).zip \
		$(WEBSITE)/ptolemy/plot/
	(cd $(WEBSITE)/ptolemy/plot/doc/download; \
		ln -s ../../../../../../ptolemyII/ptII1.0/j2re-1_3_0_01-win.exe  .)

