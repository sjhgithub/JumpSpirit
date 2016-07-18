package cn.mailchat.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.cache.TemporaryAttachmentStore;
import cn.mailchat.provider.AttachmentProvider;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 文件工具类
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-9-6
 */
@TargetApi(19)
public class FileUtil {

	private static final String TAG = FileUtil.class.getSimpleName(); 
	public static final String DEFAULT_ATTACHMENT_MIME_TYPE = "application/octet-stream";
	public static final String SETTINGS_MIME_TYPE = "application/x-wemailsettings";
	public static final String[][] MIME_TYPE_BY_EXTENSION_MAP = new String[][] {
			// * Do not delete the next two lines
	{ "", DEFAULT_ATTACHMENT_MIME_TYPE }, { "wemail", SETTINGS_MIME_TYPE },
			// * Do not delete the previous two lines
	{ "123", "application/vnd.lotus-1-2-3" }, { "323", "text/h323" }, { "3dml", "text/vnd.in3d.3dml" }, { "3g2", "video/3gpp2" }, { "3gp", "video/3gpp" }, { "aab", "application/x-authorware-bin" }, { "aac", "audio/x-aac" }, { "aam", "application/x-authorware-map" }, { "a", "application/octet-stream" }, { "aas", "application/x-authorware-seg" }, { "abw", "application/x-abiword" }, { "acc", "application/vnd.americandynamics.acc" }, { "ace", "application/x-ace-compressed" }, { "acu", "application/vnd.acucobol" }, { "acutc", "application/vnd.acucorp" }, { "acx", "application/internet-property-stream" }, { "adp", "audio/adpcm" }, { "aep", "application/vnd.audiograph" }, { "afm", "application/x-font-type1" }, { "afp", "application/vnd.ibm.modcap" }, { "ai", "application/postscript" }, { "aif", "audio/x-aiff" }, { "aifc", "audio/x-aiff" }, { "aiff", "audio/x-aiff" }, { "air", "application/vnd.adobe.air-application-installer-package+zip" }, { "ami", "application/vnd.amiga.ami" }, { "apk", "application/vnd.android.package-archive" }, { "application", "application/x-ms-application" }, { "apr", "application/vnd.lotus-approach" }, { "asc", "application/pgp-signature" }, { "asf", "video/x-ms-asf" }, { "asm", "text/x-asm" }, { "aso", "application/vnd.accpac.simply.aso" }, { "asr", "video/x-ms-asf" }, { "asx", "video/x-ms-asf" }, { "atc", "application/vnd.acucorp" }, { "atom", "application/atom+xml" }, { "atomcat", "application/atomcat+xml" }, { "atomsvc", "application/atomsvc+xml" }, { "atx", "application/vnd.antix.game-component" }, { "au", "audio/basic" }, { "avi", "video/x-msvideo" }, { "aw", "application/applixware" }, { "axs", "application/olescript" }, { "azf", "application/vnd.airzip.filesecure.azf" }, { "azs", "application/vnd.airzip.filesecure.azs" }, { "azw", "application/vnd.amazon.ebook" }, { "bas", "text/plain" }, { "bat", "application/x-msdownload" }, { "bcpio", "application/x-bcpio" }, { "bdf", "application/x-font-bdf" }, { "bdm", "application/vnd.syncml.dm+wbxml" }, { "bh2", "application/vnd.fujitsu.oasysprs" }, { "bin", "application/octet-stream" }, { "bmi", "application/vnd.bmi" }, { "bmp", "image/bmp" }, { "book", "application/vnd.framemaker" }, { "box", "application/vnd.previewsystems.box" }, { "boz", "application/x-bzip2" }, { "bpk", "application/octet-stream" }, { "btif", "image/prs.btif" }, { "bz2", "application/x-bzip2" }, { "bz", "application/x-bzip" }, { "c4d", "application/vnd.clonk.c4group" }, { "c4f", "application/vnd.clonk.c4group" }, { "c4g", "application/vnd.clonk.c4group" }, { "c4p", "application/vnd.clonk.c4group" }, { "c4u", "application/vnd.clonk.c4group" }, { "cab", "application/vnd.ms-cab-compressed" }, { "car", "application/vnd.curl.car" }, { "cat", "application/vnd.ms-pki.seccat" }, { "cct", "application/x-director" }, { "cc", "text/x-c" }, { "ccxml", "application/ccxml+xml" }, { "cdbcmsg", "application/vnd.contact.cmsg" }, { "cdf", "application/x-cdf" }, { "cdkey", "application/vnd.mediastation.cdkey" }, { "cdx", "chemical/x-cdx" }, { "cdxml", "application/vnd.chemdraw+xml" }, { "cdy", "application/vnd.cinderella" }, { "cer", "application/x-x509-ca-cert" }, { "cgm", "image/cgm" }, { "chat", "application/x-chat" }, { "chm", "application/vnd.ms-htmlhelp" }, { "chrt", "application/vnd.kde.kchart" }, { "cif", "chemical/x-cif" }, { "cii", "application/vnd.anser-web-certificate-issue-initiation" }, { "cla", "application/vnd.claymore" }, { "class", "application/java-vm" }, { "clkk", "application/vnd.crick.clicker.keyboard" }, { "clkp", "application/vnd.crick.clicker.palette" }, { "clkt", "application/vnd.crick.clicker.template" }, { "clkw", "application/vnd.crick.clicker.wordbank" }, { "clkx", "application/vnd.crick.clicker" }, { "clp", "application/x-msclip" }, { "cmc", "application/vnd.cosmocaller" }, { "cmdf", "chemical/x-cmdf" }, { "cml", "chemical/x-cml" }, { "cmp", "application/vnd.yellowriver-custom-menu" }, { "cmx", "image/x-cmx" }, { "cod", "application/vnd.rim.cod" }, { "com", "application/x-msdownload" }, { "conf", "text/plain" }, { "cpio", "application/x-cpio" }, { "cpp", "text/x-c" }, { "cpt", "application/mac-compactpro" }, { "crd", "application/x-mscardfile" }, { "crl", "application/pkix-crl" }, { "crt", "application/x-x509-ca-cert" }, { "csh", "application/x-csh" }, { "csml", "chemical/x-csml" }, { "csp", "application/vnd.commonspace" }, { "css", "text/css" }, { "cst", "application/x-director" }, { "csv", "text/csv" }, { "c", "text/plain" }, { "cu", "application/cu-seeme" }, { "curl", "text/vnd.curl" }, { "cww", "application/prs.cww" }, { "cxt", "application/x-director" }, { "cxx", "text/x-c" }, { "daf", "application/vnd.mobius.daf" }, { "dataless", "application/vnd.fdsn.seed" }, { "davmount", "application/davmount+xml" }, { "dcr", "application/x-director" }, { "dcurl", "text/vnd.curl.dcurl" }, { "dd2", "application/vnd.oma.dd2+xml" }, { "ddd", "application/vnd.fujixerox.ddd" }, { "deb", "application/x-debian-package" }, { "def", "text/plain" }, { "deploy", "application/octet-stream" }, { "der", "application/x-x509-ca-cert" }, { "dfac", "application/vnd.dreamfactory" }, { "dic", "text/x-c" }, { "diff", "text/plain" }, { "dir", "application/x-director" }, { "dis", "application/vnd.mobius.dis" }, { "dist", "application/octet-stream" }, { "distz", "application/octet-stream" }, { "djv", "image/vnd.djvu" }, { "djvu", "image/vnd.djvu" }, { "dll", "application/x-msdownload" }, { "dmg", "application/octet-stream" }, { "dms", "application/octet-stream" }, { "dna", "application/vnd.dna" }, { "doc", "application/msword" }, { "docm", "application/vnd.ms-word.document.macroenabled.12" }, { "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" }, { "dot", "application/msword" }, { "dotm", "application/vnd.ms-word.template.macroenabled.12" }, { "dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template" }, { "dp", "application/vnd.osgi.dp" }, { "dpg", "application/vnd.dpgraph" }, { "dsc", "text/prs.lines.tag" }, { "dtb", "application/x-dtbook+xml" }, { "dtd", "application/xml-dtd" }, { "dts", "audio/vnd.dts" }, { "dtshd", "audio/vnd.dts.hd" }, { "dump", "application/octet-stream" }, { "dvi", "application/x-dvi" }, { "dwf", "model/vnd.dwf" }, { "dwg", "image/vnd.dwg" }, { "dxf", "image/vnd.dxf" }, { "dxp", "application/vnd.spotfire.dxp" }, { "dxr", "application/x-director" }, { "ecelp4800", "audio/vnd.nuera.ecelp4800" }, { "ecelp7470", "audio/vnd.nuera.ecelp7470" }, { "ecelp9600", "audio/vnd.nuera.ecelp9600" }, { "ecma", "application/ecmascript" }, { "edm", "application/vnd.novadigm.edm" }, { "edx", "application/vnd.novadigm.edx" }, { "efif", "application/vnd.picsel" }, { "ei6", "application/vnd.pg.osasli" }, { "elc", "application/octet-stream" }, { "eml", "message/rfc822" }, { "emma", "application/emma+xml" }, { "eol", "audio/vnd.digital-winds" }, { "eot", "application/vnd.ms-fontobject" }, { "eps", "application/postscript" }, { "epub", "application/epub+zip" }, { "es3", "application/vnd.eszigno3+xml" }, { "esf", "application/vnd.epson.esf" }, { "et3", "application/vnd.eszigno3+xml" }, { "etx", "text/x-setext" }, { "evy", "application/envoy" }, { "exe", "application/octet-stream" }, { "ext", "application/vnd.novadigm.ext" }, { "ez2", "application/vnd.ezpix-album" }, { "ez3", "application/vnd.ezpix-package" }, { "ez", "application/andrew-inset" }, { "f4v", "video/x-f4v" }, { "f77", "text/x-fortran" }, { "f90", "text/x-fortran" }, { "fbs", "image/vnd.fastbidsheet" }, { "fdf", "application/vnd.fdf" }, { "fe_launch", "application/vnd.denovo.fcselayout-link" }, { "fg5", "application/vnd.fujitsu.oasysgp" }, { "fgd", "application/x-director" }, { "fh4", "image/x-freehand" }, { "fh5", "image/x-freehand" }, { "fh7", "image/x-freehand" }, { "fhc", "image/x-freehand" }, { "fh", "image/x-freehand" }, { "fif", "application/fractals" }, { "fig", "application/x-xfig" }, { "fli", "video/x-fli" }, { "flo", "application/vnd.micrografx.flo" }, { "flr", "x-world/x-vrml" }, { "flv", "video/x-flv" }, { "flw", "application/vnd.kde.kivio" }, { "flx", "text/vnd.fmi.flexstor" }, { "fly", "text/vnd.fly" }, { "fm", "application/vnd.framemaker" }, { "fnc", "application/vnd.frogans.fnc" }, { "for", "text/x-fortran" }, { "fpx", "image/vnd.fpx" }, { "frame", "application/vnd.framemaker" }, { "fsc", "application/vnd.fsc.weblaunch" }, { "fst", "image/vnd.fst" }, { "ftc", "application/vnd.fluxtime.clip" }, { "f", "text/x-fortran" }, { "fti", "application/vnd.anser-web-funds-transfer-initiation" }, { "fvt", "video/vnd.fvt" }, { "fzs", "application/vnd.fuzzysheet" }, { "g3", "image/g3fax" }, { "gac", "application/vnd.groove-account" }, { "gdl", "model/vnd.gdl" }, { "geo", "application/vnd.dynageo" }, { "gex", "application/vnd.geometry-explorer" }, { "ggb", "application/vnd.geogebra.file" }, { "ggt", "application/vnd.geogebra.tool" }, { "ghf", "application/vnd.groove-help" }, { "gif", "image/gif" }, { "gim", "application/vnd.groove-identity-message" }, { "gmx", "application/vnd.gmx" }, { "gnumeric", "application/x-gnumeric" }, { "gph", "application/vnd.flographit" }, { "gqf", "application/vnd.grafeq" }, { "gqs", "application/vnd.grafeq" }, { "gram", "application/srgs" }, { "gre", "application/vnd.geometry-explorer" }, { "grv", "application/vnd.groove-injector" }, { "grxml", "application/srgs+xml" }, { "gsf", "application/x-font-ghostscript" }, { "gtar", "application/x-gtar" }, { "gtm", "application/vnd.groove-tool-message" }, { "gtw", "model/vnd.gtw" }, { "gv", "text/vnd.graphviz" }, { "gz", "application/x-gzip" }, { "h261", "video/h261" }, { "h263", "video/h263" }, { "h264", "video/h264" }, { "hbci", "application/vnd.hbci" }, { "hdf", "application/x-hdf" }, { "hh", "text/x-c" }, { "hlp", "application/winhlp" }, { "hpgl", "application/vnd.hp-hpgl" }, { "hpid", "application/vnd.hp-hpid" }, { "hps", "application/vnd.hp-hps" }, { "hqx", "application/mac-binhex40" }, { "hta", "application/hta" }, { "htc", "text/x-component" }, { "h", "text/plain" }, { "htke", "application/vnd.kenameaapp" }, { "html", "text/html" }, { "htm", "text/html" }, { "htt", "text/webviewhtml" }, { "hvd", "application/vnd.yamaha.hv-dic" }, { "hvp", "application/vnd.yamaha.hv-voice" }, { "hvs", "application/vnd.yamaha.hv-script" }, { "icc", "application/vnd.iccprofile" }, { "ice", "x-conference/x-cooltalk" }, { "icm", "application/vnd.iccprofile" }, { "ico", "image/x-icon" }, { "ics", "text/calendar" }, { "ief", "image/ief" }, { "ifb", "text/calendar" }, { "ifm", "application/vnd.shana.informed.formdata" }, { "iges", "model/iges" }, { "igl", "application/vnd.igloader" }, { "igs", "model/iges" }, { "igx", "application/vnd.micrografx.igx" }, { "iif", "application/vnd.shana.informed.interchange" }, { "iii", "application/x-iphone" }, { "imp", "application/vnd.accpac.simply.imp" }, { "ims", "application/vnd.ms-ims" }, { "ins", "application/x-internet-signup" }, { "in", "text/plain" }, { "ipk", "application/vnd.shana.informed.package" }, { "irm", "application/vnd.ibm.rights-management" }, { "irp", "application/vnd.irepository.package+xml" }, { "iso", "application/octet-stream" }, { "isp", "application/x-internet-signup" }, { "itp", "application/vnd.shana.informed.formtemplate" }, { "ivp", "application/vnd.immervision-ivp" }, { "ivu", "application/vnd.immervision-ivu" }, { "jad", "text/vnd.sun.j2me.app-descriptor" }, { "jam", "application/vnd.jam" }, { "jar", "application/java-archive" }, { "java", "text/x-java-source" }, { "jfif", "image/pipeg" }, { "jisp", "application/vnd.jisp" }, { "jlt", "application/vnd.hp-jlyt" }, { "jnlp", "application/x-java-jnlp-file" }, { "joda", "application/vnd.joost.joda-archive" }, { "jpeg", "image/jpeg" }, { "jpe", "image/jpeg" }, { "jpg", "image/jpeg" }, { "jpgm", "video/jpm" }, { "jpgv", "video/jpeg" }, { "jpm", "video/jpm" }, { "js", "application/x-javascript" }, { "json", "application/json" }, { "kar", "audio/midi" }, { "karbon", "application/vnd.kde.karbon" }, { "kfo", "application/vnd.kde.kformula" }, { "kia", "application/vnd.kidspiration" }, { "kil", "application/x-killustrator" }, { "kml", "application/vnd.google-earth.kml+xml" }, { "kmz", "application/vnd.google-earth.kmz" }, { "kne", "application/vnd.kinar" }, { "knp", "application/vnd.kinar" }, { "kon", "application/vnd.kde.kontour" }, { "kpr", "application/vnd.kde.kpresenter" }, { "kpt", "application/vnd.kde.kpresenter" }, { "ksh", "text/plain" }, { "ksp", "application/vnd.kde.kspread" }, { "ktr", "application/vnd.kahootz" }, { "ktz", "application/vnd.kahootz" }, { "kwd", "application/vnd.kde.kword" }, { "kwt", "application/vnd.kde.kword" }, { "latex", "application/x-latex" }, { "lbd", "application/vnd.llamagraphics.life-balance.desktop" }, { "lbe", "application/vnd.llamagraphics.life-balance.exchange+xml" }, { "les", "application/vnd.hhe.lesson-player" }, { "lha", "application/octet-stream" }, { "link66", "application/vnd.route66.link66+xml" }, { "list3820", "application/vnd.ibm.modcap" }, { "listafp", "application/vnd.ibm.modcap" }, { "list", "text/plain" }, { "log", "text/plain" }, { "lostxml", "application/lost+xml" }, { "lrf", "application/octet-stream" }, { "lrm", "application/vnd.ms-lrm" }, { "lsf", "video/x-la-asf" }, { "lsx", "video/x-la-asf" }, { "ltf", "application/vnd.frogans.ltf" }, { "lvp", "audio/vnd.lucent.voice" }, { "lwp", "application/vnd.lotus-wordpro" }, { "lzh", "application/octet-stream" }, { "m13", "application/x-msmediaview" }, { "m14", "application/x-msmediaview" }, { "m1v", "video/mpeg" }, { "m2a", "audio/mpeg" }, { "m2v", "video/mpeg" }, { "m3a", "audio/mpeg" }, { "m3u", "audio/x-mpegurl" }, { "m4u", "video/vnd.mpegurl" }, { "m4v", "video/x-m4v" }, { "ma", "application/mathematica" }, { "mag", "application/vnd.ecowin.chart" }, { "maker", "application/vnd.framemaker" }, { "man", "text/troff" }, { "mathml", "application/mathml+xml" }, { "mb", "application/mathematica" }, { "mbk", "application/vnd.mobius.mbk" }, { "mbox", "application/mbox" }, { "mc1", "application/vnd.medcalcdata" }, { "mcd", "application/vnd.mcd" }, { "mcurl", "text/vnd.curl.mcurl" }, { "mdb", "application/x-msaccess" }, { "mdi", "image/vnd.ms-modi" }, { "mesh", "model/mesh" }, { "me", "text/troff" }, { "mfm", "application/vnd.mfmp" }, { "mgz", "application/vnd.proteus.magazine" }, { "mht", "message/rfc822" }, { "mhtml", "message/rfc822" }, { "mid", "audio/midi" }, { "midi", "audio/midi" }, { "mif", "application/vnd.mif" }, { "mime", "message/rfc822" }, { "mj2", "video/mj2" }, { "mjp2", "video/mj2" }, { "mlp", "application/vnd.dolby.mlp" }, { "mmd", "application/vnd.chipnuts.karaoke-mmd" }, { "mmf", "application/vnd.smaf" }, { "mmr", "image/vnd.fujixerox.edmics-mmr" }, { "mny", "application/x-msmoney" }, { "mobi", "application/x-mobipocket-ebook" }, { "movie", "video/x-sgi-movie" }, { "mov", "video/quicktime" }, { "mp2a", "audio/mpeg" }, { "mp2", "video/mpeg" }, { "mp3", "audio/mpeg" }, { "mp4a", "audio/mp4" }, { "mp4s", "application/mp4" }, { "mp4", "video/mp4" }, { "mp4v", "video/mp4" }, { "mpa", "video/mpeg" }, { "mpc", "application/vnd.mophun.certificate" }, { "mpeg", "video/mpeg" }, { "mpe", "video/mpeg" }, { "mpg4", "video/mp4" }, { "mpga", "audio/mpeg" }, { "mpg", "video/mpeg" }, { "mpkg", "application/vnd.apple.installer+xml" }, { "mpm", "application/vnd.blueice.multipass" }, { "mpn", "application/vnd.mophun.application" }, { "mpp", "application/vnd.ms-project" }, { "mpt", "application/vnd.ms-project" }, { "mpv2", "video/mpeg" }, { "mpy", "application/vnd.ibm.minipay" }, { "mqy", "application/vnd.mobius.mqy" }, { "mrc", "application/marc" }, { "mscml", "application/mediaservercontrol+xml" }, { "mseed", "application/vnd.fdsn.mseed" }, { "mseq", "application/vnd.mseq" }, { "msf", "application/vnd.epson.msf" }, { "msh", "model/mesh" }, { "msi", "application/x-msdownload" }, { "ms", "text/troff" }, { "msty", "application/vnd.muvee.style" }, { "mts", "model/vnd.mts" }, { "mus", "application/vnd.musician" }, { "musicxml", "application/vnd.recordare.musicxml+xml" }, { "mvb", "application/x-msmediaview" }, { "mxf", "application/mxf" }, { "mxl", "application/vnd.recordare.musicxml" }, { "mxml", "application/xv+xml" }, { "mxs", "application/vnd.triscape.mxs" }, { "mxu", "video/vnd.mpegurl" }, { "nb", "application/mathematica" }, { "nc", "application/x-netcdf" }, { "ncx", "application/x-dtbncx+xml" }, { "n-gage", "application/vnd.nokia.n-gage.symbian.install" }, { "ngdat", "application/vnd.nokia.n-gage.data" }, { "nlu", "application/vnd.neurolanguage.nlu" }, { "nml", "application/vnd.enliven" }, { "nnd", "application/vnd.noblenet-directory" }, { "nns", "application/vnd.noblenet-sealer" }, { "nnw", "application/vnd.noblenet-web" }, { "npx", "image/vnd.net-fpx" }, { "nsf", "application/vnd.lotus-notes" }, { "nws", "message/rfc822" }, { "oa2", "application/vnd.fujitsu.oasys2" }, { "oa3", "application/vnd.fujitsu.oasys3" }, { "o", "application/octet-stream" }, { "oas", "application/vnd.fujitsu.oasys" }, { "obd", "application/x-msbinder" }, { "obj", "application/octet-stream" }, { "oda", "application/oda" }, { "odb", "application/vnd.oasis.opendocument.database" }, { "odc", "application/vnd.oasis.opendocument.chart" }, { "odf", "application/vnd.oasis.opendocument.formula" }, { "odft", "application/vnd.oasis.opendocument.formula-template" }, { "odg", "application/vnd.oasis.opendocument.graphics" }, { "odi", "application/vnd.oasis.opendocument.image" }, { "odp", "application/vnd.oasis.opendocument.presentation" }, { "ods", "application/vnd.oasis.opendocument.spreadsheet" }, { "odt", "application/vnd.oasis.opendocument.text" }, { "oga", "audio/ogg" }, { "ogg", "audio/ogg" }, { "ogv", "video/ogg" }, { "ogx", "application/ogg" }, { "onepkg", "application/onenote" }, { "onetmp", "application/onenote" }, { "onetoc2", "application/onenote" }, { "onetoc", "application/onenote" }, { "opf", "application/oebps-package+xml" }, { "oprc", "application/vnd.palm" }, { "org", "application/vnd.lotus-organizer" }, { "osf", "application/vnd.yamaha.openscoreformat" }, { "osfpvg", "application/vnd.yamaha.openscoreformat.osfpvg+xml" }, { "otc", "application/vnd.oasis.opendocument.chart-template" }, { "otf", "application/x-font-otf" }, { "otg", "application/vnd.oasis.opendocument.graphics-template" }, { "oth", "application/vnd.oasis.opendocument.text-web" }, { "oti", "application/vnd.oasis.opendocument.image-template" }, { "otm", "application/vnd.oasis.opendocument.text-master" }, { "otp", "application/vnd.oasis.opendocument.presentation-template" }, { "ots", "application/vnd.oasis.opendocument.spreadsheet-template" }, { "ott", "application/vnd.oasis.opendocument.text-template" }, { "oxt", "application/vnd.openofficeorg.extension" }, { "p10", "application/pkcs10" }, { "p12", "application/x-pkcs12" }, { "p7b", "application/x-pkcs7-certificates" }, { "p7c", "application/x-pkcs7-mime" }, { "p7m", "application/x-pkcs7-mime" }, { "p7r", "application/x-pkcs7-certreqresp" }, { "p7s", "application/x-pkcs7-signature" }, { "pas", "text/x-pascal" }, { "pbd", "application/vnd.powerbuilder6" }, { "pbm", "image/x-portable-bitmap" }, { "pcf", "application/x-font-pcf" }, { "pcl", "application/vnd.hp-pcl" }, { "pclxl", "application/vnd.hp-pclxl" }, { "pct", "image/x-pict" }, { "pcurl", "application/vnd.curl.pcurl" }, { "pcx", "image/x-pcx" }, { "pdb", "application/vnd.palm" }, { "pdf", "application/pdf" }, { "pfa", "application/x-font-type1" }, { "pfb", "application/x-font-type1" }, { "pfm", "application/x-font-type1" }, { "pfr", "application/font-tdpfr" }, { "pfx", "application/x-pkcs12" }, { "pgm", "image/x-portable-graymap" }, { "pgn", "application/x-chess-pgn" }, { "pgp", "application/pgp-encrypted" }, { "pic", "image/x-pict" }, { "pkg", "application/octet-stream" }, { "pki", "application/pkixcmp" }, { "pkipath", "application/pkix-pkipath" }, { "pko", "application/ynd.ms-pkipko" }, { "plb", "application/vnd.3gpp.pic-bw-large" }, { "plc", "application/vnd.mobius.plc" }, { "plf", "application/vnd.pocketlearn" }, { "pls", "application/pls+xml" }, { "pl", "text/plain" }, { "pma", "application/x-perfmon" }, { "pmc", "application/x-perfmon" }, { "pml", "application/x-perfmon" }, { "pmr", "application/x-perfmon" }, { "pmw", "application/x-perfmon" }, { "png", "image/png" }, { "pnm", "image/x-portable-anymap" }, { "portpkg", "application/vnd.macports.portpkg" }, { "pot,", "application/vnd.ms-powerpoint" }, { "pot", "application/vnd.ms-powerpoint" }, { "potm", "application/vnd.ms-powerpoint.template.macroenabled.12" }, { "potx", "application/vnd.openxmlformats-officedocument.presentationml.template" }, { "ppa", "application/vnd.ms-powerpoint" }, { "ppam", "application/vnd.ms-powerpoint.addin.macroenabled.12" }, { "ppd", "application/vnd.cups-ppd" }, { "ppm", "image/x-portable-pixmap" }, { "pps", "application/vnd.ms-powerpoint" }, { "ppsm", "application/vnd.ms-powerpoint.slideshow.macroenabled.12" }, { "ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow" }, { "ppt", "application/vnd.ms-powerpoint" }, { "pptm", "application/vnd.ms-powerpoint.presentation.macroenabled.12" }, { "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation" }, { "pqa", "application/vnd.palm" }, { "prc", "application/x-mobipocket-ebook" }, { "pre", "application/vnd.lotus-freelance" }, { "prf", "application/pics-rules" }, { "ps", "application/postscript" }, { "psb", "application/vnd.3gpp.pic-bw-small" }, { "psd", "image/vnd.adobe.photoshop" }, { "psf", "application/x-font-linux-psf" }, { "p", "text/x-pascal" }, { "ptid", "application/vnd.pvi.ptid1" }, { "pub", "application/x-mspublisher" }, { "pvb", "application/vnd.3gpp.pic-bw-var" }, { "pwn", "application/vnd.3m.post-it-notes" }, { "pwz", "application/vnd.ms-powerpoint" }, { "pya", "audio/vnd.ms-playready.media.pya" }, { "pyc", "application/x-python-code" }, { "pyo", "application/x-python-code" }, { "py", "text/x-python" }, { "pyv", "video/vnd.ms-playready.media.pyv" }, { "qam", "application/vnd.epson.quickanime" }, { "qbo", "application/vnd.intu.qbo" }, { "qfx", "application/vnd.intu.qfx" }, { "qps", "application/vnd.publishare-delta-tree" }, { "qt", "video/quicktime" }, { "qwd", "application/vnd.quark.quarkxpress" }, { "qwt", "application/vnd.quark.quarkxpress" }, { "qxb", "application/vnd.quark.quarkxpress" }, { "qxd", "application/vnd.quark.quarkxpress" }, { "qxl", "application/vnd.quark.quarkxpress" }, { "qxt", "application/vnd.quark.quarkxpress" }, { "ra", "audio/x-pn-realaudio" }, { "ram", "audio/x-pn-realaudio" }, { "rar", "application/x-rar-compressed" }, { "ras", "image/x-cmu-raster" }, { "rcprofile", "application/vnd.ipunplugged.rcprofile" }, { "rdf", "application/rdf+xml" }, { "rdz", "application/vnd.data-vision.rdz" }, { "rep", "application/vnd.businessobjects" }, { "res", "application/x-dtbresource+xml" }, { "rgb", "image/x-rgb" }, { "rif", "application/reginfo+xml" }, { "rl", "application/resource-lists+xml" }, { "rlc", "image/vnd.fujixerox.edmics-rlc" }, { "rld", "application/resource-lists-diff+xml" }, { "rm", "application/vnd.rn-realmedia" }, { "rmi", "audio/midi" }, { "rmp", "audio/x-pn-realaudio-plugin" }, { "rms", "application/vnd.jcp.javame.midlet-rms" }, { "rnc", "application/relax-ng-compact-syntax" }, { "roff", "text/troff" }, { "rpm", "application/x-rpm" }, { "rpss", "application/vnd.nokia.radio-presets" }, { "rpst", "application/vnd.nokia.radio-preset" }, { "rq", "application/sparql-query" }, { "rs", "application/rls-services+xml" }, { "rsd", "application/rsd+xml" }, { "rss", "application/rss+xml" }, { "rtf", "application/rtf" }, { "rtx", "text/richtext" }, { "saf", "application/vnd.yamaha.smaf-audio" }, { "sbml", "application/sbml+xml" }, { "sc", "application/vnd.ibm.secure-container" }, { "scd", "application/x-msschedule" }, { "scm", "application/vnd.lotus-screencam" }, { "scq", "application/scvp-cv-request" }, { "scs", "application/scvp-cv-response" }, { "sct", "text/scriptlet" }, { "scurl", "text/vnd.curl.scurl" }, { "sda", "application/vnd.stardivision.draw" }, { "sdc", "application/vnd.stardivision.calc" }, { "sdd", "application/vnd.stardivision.impress" }, { "sdkd", "application/vnd.solent.sdkm+xml" }, { "sdkm", "application/vnd.solent.sdkm+xml" }, { "sdp", "application/sdp" }, { "sdw", "application/vnd.stardivision.writer" }, { "see", "application/vnd.seemail" }, { "seed", "application/vnd.fdsn.seed" }, { "sema", "application/vnd.sema" }, { "semd", "application/vnd.semd" }, { "semf", "application/vnd.semf" }, { "ser", "application/java-serialized-object" }, { "setpay", "application/set-payment-initiation" }, { "setreg", "application/set-registration-initiation" }, { "sfd-hdstx", "application/vnd.hydrostatix.sof-data" }, { "sfs", "application/vnd.spotfire.sfs" }, { "sgl", "application/vnd.stardivision.writer-global" }, { "sgml", "text/sgml" }, { "sgm", "text/sgml" }, { "sh", "application/x-sh" }, { "shar", "application/x-shar" }, { "shf", "application/shf+xml" }, { "sic", "application/vnd.wap.sic" }, { "sig", "application/pgp-signature" }, { "silo", "model/mesh" }, { "sis", "application/vnd.symbian.install" }, { "sisx", "application/vnd.symbian.install" }, { "sit", "application/x-stuffit" }, { "si", "text/vnd.wap.si" }, { "sitx", "application/x-stuffitx" }, { "skd", "application/vnd.koan" }, { "skm", "application/vnd.koan" }, { "skp", "application/vnd.koan" }, { "skt", "application/vnd.koan" }, { "slc", "application/vnd.wap.slc" }, { "sldm", "application/vnd.ms-powerpoint.slide.macroenabled.12" }, { "sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide" }, { "slt", "application/vnd.epson.salt" }, { "sl", "text/vnd.wap.sl" }, { "smf", "application/vnd.stardivision.math" }, { "smi", "application/smil+xml" }, { "smil", "application/smil+xml" }, { "snd", "audio/basic" }, { "snf", "application/x-font-snf" }, { "so", "application/octet-stream" }, { "spc", "application/x-pkcs7-certificates" }, { "spf", "application/vnd.yamaha.smaf-phrase" }, { "spl", "application/x-futuresplash" }, { "spot", "text/vnd.in3d.spot" }, { "spp", "application/scvp-vp-response" }, { "spq", "application/scvp-vp-request" }, { "spx", "audio/ogg" }, { "src", "application/x-wais-source" }, { "srx", "application/sparql-results+xml" }, { "sse", "application/vnd.kodak-descriptor" }, { "ssf", "application/vnd.epson.ssf" }, { "ssml", "application/ssml+xml" }, { "sst", "application/vnd.ms-pkicertstore" }, { "stc", "application/vnd.sun.xml.calc.template" }, { "std", "application/vnd.sun.xml.draw.template" }, { "s", "text/x-asm" }, { "stf", "application/vnd.wt.stf" }, { "sti", "application/vnd.sun.xml.impress.template" }, { "stk", "application/hyperstudio" }, { "stl", "application/vnd.ms-pki.stl" }, { "stm", "text/html" }, { "str", "application/vnd.pg.format" }, { "stw", "application/vnd.sun.xml.writer.template" }, { "sus", "application/vnd.sus-calendar" }, { "susp", "application/vnd.sus-calendar" }, { "sv4cpio", "application/x-sv4cpio" }, { "sv4crc", "application/x-sv4crc" }, { "svd", "application/vnd.svd" }, { "svg", "image/svg+xml" }, { "svgz", "image/svg+xml" }, { "swa", "application/x-director" }, { "swf", "application/x-shockwave-flash" }, { "swi", "application/vnd.arastra.swi" }, { "sxc", "application/vnd.sun.xml.calc" }, { "sxd", "application/vnd.sun.xml.draw" }, { "sxg", "application/vnd.sun.xml.writer.global" }, { "sxi", "application/vnd.sun.xml.impress" }, { "sxm", "application/vnd.sun.xml.math" }, { "sxw", "application/vnd.sun.xml.writer" }, { "tao", "application/vnd.tao.intent-module-archive" }, { "t", "application/x-troff" }, { "tar", "application/x-tar" }, { "tcap", "application/vnd.3gpp2.tcap" }, { "tcl", "application/x-tcl" }, { "teacher", "application/vnd.smart.teacher" }, { "tex", "application/x-tex" }, { "texi", "application/x-texinfo" }, { "texinfo", "application/x-texinfo" }, { "text", "text/plain" }, { "tfm", "application/x-tex-tfm" }, { "tgz", "application/x-gzip" }, { "tiff", "image/tiff" }, { "tif", "image/tiff" }, { "tmo", "application/vnd.tmobile-livetv" }, { "torrent", "application/x-bittorrent" }, { "tpl", "application/vnd.groove-tool-template" }, { "tpt", "application/vnd.trid.tpt" }, { "tra", "application/vnd.trueapp" }, { "trm", "application/x-msterminal" }, { "tr", "text/troff" }, { "tsv", "text/tab-separated-values" }, { "ttc", "application/x-font-ttf" }, { "ttf", "application/x-font-ttf" }, { "twd", "application/vnd.simtech-mindmapper" }, { "twds", "application/vnd.simtech-mindmapper" }, { "txd", "application/vnd.genomatix.tuxedo" }, { "txf", "application/vnd.mobius.txf" }, { "txt", "text/plain" }, { "u32", "application/x-authorware-bin" }, { "udeb", "application/x-debian-package" }, { "ufd", "application/vnd.ufdl" }, { "ufdl", "application/vnd.ufdl" }, { "uls", "text/iuls" }, { "umj", "application/vnd.umajin" }, { "unityweb", "application/vnd.unity" }, { "uoml", "application/vnd.uoml+xml" }, { "uris", "text/uri-list" }, { "uri", "text/uri-list" }, { "urls", "text/uri-list" }, { "ustar", "application/x-ustar" }, { "utz", "application/vnd.uiq.theme" }, { "uu", "text/x-uuencode" }, { "vcd", "application/x-cdlink" }, { "vcf", "text/x-vcard" }, { "vcg", "application/vnd.groove-vcard" }, { "vcs", "text/x-vcalendar" }, { "vcx", "application/vnd.vcx" }, { "vis", "application/vnd.visionary" }, { "viv", "video/vnd.vivo" }, { "vor", "application/vnd.stardivision.writer" }, { "vox", "application/x-authorware-bin" }, { "vrml", "x-world/x-vrml" }, { "vsd", "application/vnd.visio" }, { "vsf", "application/vnd.vsf" }, { "vss", "application/vnd.visio" }, { "vst", "application/vnd.visio" }, { "vsw", "application/vnd.visio" }, { "vtu", "model/vnd.vtu" }, { "vxml", "application/voicexml+xml" }, { "w3d", "application/x-director" }, { "wad", "application/x-doom" }, { "wav", "audio/x-wav" }, { "wax", "audio/x-ms-wax" }, { "wbmp", "image/vnd.wap.wbmp" }, { "wbs", "application/vnd.criticaltools.wbs+xml" }, { "wbxml", "application/vnd.wap.wbxml" }, { "wcm", "application/vnd.ms-works" }, { "wdb", "application/vnd.ms-works" }, { "wiz", "application/msword" }, { "wks", "application/vnd.ms-works" }, { "wma", "audio/x-ms-wma" }, { "wmd", "application/x-ms-wmd" }, { "wmf", "application/x-msmetafile" }, { "wmlc", "application/vnd.wap.wmlc" }, { "wmlsc", "application/vnd.wap.wmlscriptc" }, { "wmls", "text/vnd.wap.wmlscript" }, { "wml", "text/vnd.wap.wml" }, { "wm", "video/x-ms-wm" }, { "wmv", "video/x-ms-wmv" }, { "wmx", "video/x-ms-wmx" }, { "wmz", "application/x-ms-wmz" }, { "wpd", "application/vnd.wordperfect" }, { "wpl", "application/vnd.ms-wpl" }, { "wps", "application/vnd.ms-works" }, { "wqd", "application/vnd.wqd" }, { "wri", "application/x-mswrite" }, { "wrl", "x-world/x-vrml" }, { "wrz", "x-world/x-vrml" }, { "wsdl", "application/wsdl+xml" }, { "wspolicy", "application/wspolicy+xml" }, { "wtb", "application/vnd.webturbo" }, { "wvx", "video/x-ms-wvx" }, { "x32", "application/x-authorware-bin" }, { "x3d", "application/vnd.hzn-3d-crossword" }, { "xaf", "x-world/x-vrml" }, { "xap", "application/x-silverlight-app" }, { "xar", "application/vnd.xara" }, { "xbap", "application/x-ms-xbap" }, { "xbd", "application/vnd.fujixerox.docuworks.binder" }, { "xbm", "image/x-xbitmap" }, { "xdm", "application/vnd.syncml.dm+xml" }, { "xdp", "application/vnd.adobe.xdp+xml" }, { "xdw", "application/vnd.fujixerox.docuworks" }, { "xenc", "application/xenc+xml" }, { "xer", "application/patch-ops-error+xml" }, { "xfdf", "application/vnd.adobe.xfdf" }, { "xfdl", "application/vnd.xfdl" }, { "xht", "application/xhtml+xml" }, { "xhtml", "application/xhtml+xml" }, { "xhvml", "application/xv+xml" }, { "xif", "image/vnd.xiff" }, { "xla", "application/vnd.ms-excel" }, { "xlam", "application/vnd.ms-excel.addin.macroenabled.12" }, { "xlb", "application/vnd.ms-excel" }, { "xlc", "application/vnd.ms-excel" }, { "xlm", "application/vnd.ms-excel" }, { "xls", "application/vnd.ms-excel" }, { "xlsb", "application/vnd.ms-excel.sheet.binary.macroenabled.12" }, { "xlsm", "application/vnd.ms-excel.sheet.macroenabled.12" }, { "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" }, { "xlt", "application/vnd.ms-excel" }, { "xltm", "application/vnd.ms-excel.template.macroenabled.12" }, { "xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template" }, { "xlw", "application/vnd.ms-excel" }, { "xml", "application/xml" }, { "xo", "application/vnd.olpc-sugar" }, { "xof", "x-world/x-vrml" }, { "xop", "application/xop+xml" }, { "xpdl", "application/xml" }, { "xpi", "application/x-xpinstall" }, { "xpm", "image/x-xpixmap" }, { "xpr", "application/vnd.is-xpr" }, { "xps", "application/vnd.ms-xpsdocument" }, { "xpw", "application/vnd.intercon.formnet" }, { "xpx", "application/vnd.intercon.formnet" }, { "xsl", "application/xml" }, { "xslt", "application/xslt+xml" }, { "xsm", "application/vnd.syncml+xml" }, { "xspf", "application/xspf+xml" }, { "xul", "application/vnd.mozilla.xul+xml" }, { "xvm", "application/xv+xml" }, { "xvml", "application/xv+xml" }, { "xwd", "image/x-xwindowdump" }, { "xyz", "chemical/x-xyz" }, { "z", "application/x-compress" }, { "zaz", "application/vnd.zzazz.deck+xml" }, { "zip", "application/zip" }, { "zir", "application/vnd.zul" }, { "zirz", "application/vnd.zul" }, { "zmm", "application/vnd.handheld-entertainment+xml" } };

	/**
	 * 复制文件
	 * 
	 * @Description:
	 * @param sourceFile
	 * @param targetFile
	 * @param replease
	 *            重名是否替换
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-9-25
	 */
	public static boolean copyFile(String sourceFile, String targetFile, boolean repleaseIfexists) {

		FileInputStream input = null;
		FileOutputStream output = null;

		File file = new File(targetFile);
		boolean existReplease = false;

		if (file.exists()) {
			if (repleaseIfexists) {
				existReplease = true;
				file = new File(targetFile + ".temp");
			} else {
				return false;
			}
		}
		try {
			input = new FileInputStream(sourceFile);
			output = new FileOutputStream(file);
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = input.read(b)) != -1) {
				output.write(b, 0, len);
			}
			output.flush();
			output.close();
			input.close();
			// 同名替换
			if (existReplease) {
				new File(targetFile).delete();
				file.renameTo(new File(targetFile));
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 删除文件，如果是目录，删除目录及目录下的所有文件
	 * 
	 * @Description:
	 * @param file
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Nov 12, 2013
	 */
	public static void deleteFile(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] subFiles = file.listFiles();
				for (File subFile : subFiles) {
					deleteFile(subFile);
				}
			}
			file.delete();
		}
	}

	/**
	 * 根据媒体文件的uri获得path
	 * 
	 * @Description:
	 * @param context
	 * @param uri
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-9-24
	 */
	public static String getMediaFilePathByUri(Context context, Uri uri) {
		String result = null;
		try {
			String alamPath = uri.toString();
			if (alamPath != null && alamPath.startsWith("content://media/")) {
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				result = cursor.getString(column_index);
				// Debug.d(TAG,"地址：" + result);
			} else {
				result = uri.getPath();
				// Debug.d(TAG,"地址：" + result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	/**
	 * 根据时间设置一个拍照时照片的文件名
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @date:2013-9-24
	 */
	public static String getCameraFileName() {
		return TimeUtils.DateFormatYYMMDDHHMMSS.format(new Date()) + ".jpg";
	}
	/**
	 * 根据时间设置一个拍照时照片的文件名
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @date:2013-9-24
	 */
	public static String getCameraFilePngName() {
		return TimeUtils.DateFormatYYMMDDHHMMSS.format(new Date()) + ".png";
	}
	/**
	 * 转换文件的大小，将文件的字节数转换为kb、mb、或gb
	 * 
	 * @Description:
	 * @param size单位byte
	 * @return保留1位小数
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-11
	 */
	public static String sizeLongToString(long size) {

		String a = "";
		if (size < 1024) {
			a = String.format("%d B", size);
		} else if (size / 1024 < 1024.0) {
			a = String.format("%.2f KB", size / 1024.0);
		} else if (size / 1048576 < 1024) {
			a = String.format("%.2f MB", size / 1048576.0);
		} else {
			a = String.format("%.2f GB", size / 1073740824.0);
		}
		return a;
	}

	/**
	 * 调用系统打开文件
	 * 
	 * @Description:
	 * @param context
	 * @param filePath
	 * @param fileName
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-9-27
	 */
	public static boolean viewFile(Context context, String filePath, String fileType, String fileName) {

		File file = new File(filePath);
		if (!file.exists()) {
			return false;
		}

		// 若服务器无法获取确定类型，则根据后缀名获取
		if (fileType == null || fileType.equals(DEFAULT_ATTACHMENT_MIME_TYPE)) {
			fileType = getMIMEType(context, null, fileName);
		}
		// 若仍不到文件类型，让用户选择打开方式
		if (fileType.equals(DEFAULT_ATTACHMENT_MIME_TYPE)) {
			fileType = "*/*";
		}

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), fileType);
		context.startActivity(Intent.createChooser(intent, context.getString(R.string.file_open_style)));
		return true;
	}

	/**
	 * 根据文件后缀名获得类型
	 * 
	 * @Description:
	 * @param fName
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-9-27
	 */
	public static String getMIMEType(Context context, String uri, String fName) {
		String type = "";

		if (!StringUtil.isEmpty(uri)) {
			type = context.getContentResolver().getType(Uri.parse(uri));
		}

		if (StringUtil.isEmpty(type)) {
			type = getMimeTypeByExtension(fName);
		}

		return type;
	}

	/**
	 * 读取表情配置文件
	 * 
	 * @Description:
	 * @param context
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-1-13
	 */
	public static List<String> getEmojiFile(Context context) {
		try {
			List<String> list = new ArrayList<String>();
			InputStream in = context.getResources().getAssets().open("emoji.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String str = null;
			while ((str = br.readLine()) != null) {
				list.add(str);
			}

			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getMimeTypeByExtension(String filename) {
		String returnedType = null;
		String extension = null;

		if (filename != null && filename.lastIndexOf('.') != -1) {
			extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.US);
			returnedType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		// If the MIME type set by the user's mailer is application/octet-stream, try to figure
		// out whether there's a sane file type extension.
		if (returnedType != null && !DEFAULT_ATTACHMENT_MIME_TYPE.equalsIgnoreCase(returnedType)) {
			return returnedType;
		} else if (extension != null) {
			for (String[] contentTypeMapEntry : MIME_TYPE_BY_EXTENSION_MAP) {
				if (contentTypeMapEntry[0].equals(extension)) {
					return contentTypeMapEntry[1];
				}
			}
		}

		return DEFAULT_ATTACHMENT_MIME_TYPE;
	}

	/**
	 * 生成唯一的文件名
	 * 
	 * @Description:
	 * @param dirPath
	 * @param fileName
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 3, 2014
	 */
	public static String generateUniqueFileName(String dirPath, String fileName) {
		String path = dirPath + fileName;
		int extentionIndex = fileName.lastIndexOf('.');
		if (extentionIndex != -1) {
			String name = fileName.substring(0, extentionIndex);
			String extention = fileName.substring(extentionIndex);
			int index = 0;
			while (true) {
				File file = new File(path);
				if (!file.exists()) {
					return path;
				}
				index++;
				path = dirPath + name + "_" + index + extention;
			}
		}
		return path;
	}

	
	// public static String selectImage1(Context context,Intent data){
		// Uri selectedImage = data.getData();
		// // Log.e(TAG, selectedImage.toString());
		// if(selectedImage!=null){
		// String uriStr=selectedImage.toString();
		// String path=uriStr.substring(10,uriStr.length());
		// //如果是系统自带的3d图库
		// if(path.startsWith("com.sec.android.gallery3d")){
		// return null;
		// }
		// }
		// String[] filePathColumn = { MediaStore.Images.Media.DATA };
		// Cursor cursor =
		// context.getContentResolver().query(selectedImage,filePathColumn, null,
		// null, null);
		// cursor.moveToFirst();
		// int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		// String picturePath = cursor.getString(columnIndex);
		// cursor.close();
		// return picturePath;
		// }
		/**
		 * 
		 * method name: getPath function @Description: TODO Parameters and return
		 * values description：
		 * 
		 * @param context
		 * @param uri
		 * @return field_name String return type
		 * @History memory：
		 * @Date：2014-8-5 下午5:41:48 @Modified by：zhangjx
		 * @Description： Get a file path from a Uri. This will get the the path for
		 *               Storage Access Framework Documents, as well as the _data
		 *               field for the MediaStore and other file-based
		 *               ContentProviders.
		 * 
		 */
		public static String getPath(final Context context, final Uri uri) {

			final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

			// DocumentProvider
			if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
				// ExternalStorageProvider
				if (isExternalStorageDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					if ("primary".equalsIgnoreCase(type)) {
						return Environment.getExternalStorageDirectory() + "/"
								+ split[1];
					}

					// TODO handle non-primary volumes
				}
				// DownloadsProvider
				else if (isDownloadsDocument(uri)) {

					final String id = DocumentsContract.getDocumentId(uri);
					final Uri contentUri = ContentUris.withAppendedId(
							Uri.parse("content://downloads/public_downloads"),
							Long.valueOf(id));

					return getDataColumn(context, contentUri, null, null);
				}
				// MediaProvider
				else if (isMediaDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					Uri contentUri = null;
					if ("image".equals(type)) {
						contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					} else if ("video".equals(type)) {
						contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					} else if ("audio".equals(type)) {
						contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					}

					final String selection = "_id=?";
					final String[] selectionArgs = new String[] { split[1] };

					return getDataColumn(context, contentUri, selection,
							selectionArgs);
				}
			}
			// MediaStore (and general)
			else if ("content".equalsIgnoreCase(uri.getScheme())) {

				// Return the remote address
				if (isGooglePhotosUri(uri))
					return uri.getLastPathSegment();

				return getDataColumn(context, uri, null, null);
			}
			// File
			else if ("file".equalsIgnoreCase(uri.getScheme())) {
				return uri.getPath();
			}

			return null;
		}

		/**
		 * Get the value of the data column for this Uri. This is useful for
		 * MediaStore Uris, and other file-based ContentProviders.
		 * 
		 * @param context
		 *            The context.
		 * @param uri
		 *            The Uri to query.
		 * @param selection
		 *            (Optional) Filter used in the query.
		 * @param selectionArgs
		 *            (Optional) Selection arguments used in the query.
		 * @return The value of the _data column, which is typically a file path.
		 */
		public static String getDataColumn(Context context, Uri uri,
				String selection, String[] selectionArgs) {

			Cursor cursor = null;
			final String column = "_data";
			final String[] projection = { column };

			try {
				cursor = context.getContentResolver().query(uri, projection,
						selection, selectionArgs, null);
				if (cursor != null && cursor.moveToFirst()) {
					final int index = cursor.getColumnIndexOrThrow(column);
					return cursor.getString(index);
				}
			} finally {
				if (cursor != null)
					cursor.close();
			}
			return null;
		}

		/**
		 * @param uri
		 *            The Uri to check.
		 * @return Whether the Uri authority is ExternalStorageProvider.
		 */
		public static boolean isExternalStorageDocument(Uri uri) {
			return "com.android.externalstorage.documents".equals(uri
					.getAuthority());
		}

		/**
		 * @param uri
		 *            The Uri to check.
		 * @return Whether the Uri authority is DownloadsProvider.
		 */
		public static boolean isDownloadsDocument(Uri uri) {
			return "com.android.providers.downloads.documents".equals(uri
					.getAuthority());
		}

		/**
		 * @param uri
		 *            The Uri to check.
		 * @return Whether the Uri authority is MediaProvider.
		 */
		public static boolean isMediaDocument(Uri uri) {
			return "com.android.providers.media.documents".equals(uri
					.getAuthority());
		}

		/**
		 * @param uri
		 *            The Uri to check.
		 * @return Whether the Uri authority is Google Photos.
		 */
		public static boolean isGooglePhotosUri(Uri uri) {
			return "com.google.android.apps.photos.content".equals(uri
					.getAuthority());
		}
		/**
		 * 关闭IO流,释放资源
		 * @param outStream
		 * @param inputStream
		 * @see: 
		 * @since: 
		 * @author: zhangqian
		 * @date:2014-1-26
		 */
		public static void safeIOStreamClose(OutputStream outStream, InputStream inputStream) {
			try {
				if(outStream != null){
					outStream.close();
				}
				if(inputStream != null){
					inputStream.close();
				}
			} catch (IOException e) {
				
			}
		}
		
		/**
		 * 等比缩放图片
		 * @author liwent
		 * @date 2014年8月18日
		 * @param bmp
		 * @param width
		 * @param height
		 * @return
		 */
		public static Bitmap getBitmapThumbnail(Bitmap src_bitmap, int width, int height) {
			// 获得图片的宽高
			int srcwidth = src_bitmap.getWidth();
			int srcheight = src_bitmap.getHeight();
			// 计算缩放比例
			float scale_width = ((float) width)/ srcwidth;
			float scale_height = ((float) height) / srcheight;
			// 取得想要缩放的matrix参数
			Matrix matrix = new Matrix();
			matrix.postScale(scale_width, scale_height);
			// 得到新的图片
			return Bitmap.createBitmap(src_bitmap, 0, 0, srcwidth, srcheight, matrix, true);
		}

		/**
		 * 
		 * @author liwent
		 * @date 2014年8月18日
		 * @param bitmap
		 * @return
		 */
		public static byte[] getBitmapByte(Bitmap bitmap) {
			if (bitmap == null) {
				return null;
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			System.out.println("[bitmap转字节码：]"+out.toByteArray());
			return out.toByteArray();
		}

	/**
	 * 检测是否有SD卡
	 * @author liwent
	 * @date 2014年8月18日
	 * @return
	 */
	public static boolean hasSdcard() {
		boolean bool = false;
		String state = Environment.getExternalStorageState();
		if (state.equals("mounted")) {
			bool = true;
		}
		return bool;
	}
	/**
	 * 保存图片到指定目录
	 * 
	 * @author liwent
	 * @date 2014年8月18日
	 * @param bitmap
	 *            图片
	 * @param aimDirectory
	 *            保存路径
	 * @param compress？压缩
	 *            ：不压缩
	 * @return success ? true : false;
	 */
	public static boolean saveImageToLocal(Bitmap bitmap, String aimDirectory, boolean compress) {
		if (bitmap == null) {
			return false;
		}
		boolean return_int = false;
		Log.v(TAG, "图片存储事件开始执行……");
		File file = new File(aimDirectory);
		if (file.exists()) {
			file.delete();
		}
		try {
			FileOutputStream stream = new FileOutputStream(file);
			if (compress) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream);
			} else {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			}
			stream.flush();
			stream.close();
			return_int = true;
			Log.i(TAG, "图片存储事件执行完毕.");
		} catch (FileNotFoundException e) {
			return_int = false;
			e.printStackTrace();
			Log.e(TAG, e.getMessage().toString());
		} catch (IOException e) {
			return_int = false;
			Log.e(TAG, e.getMessage().toString());
			e.printStackTrace();
		}
		return return_int;
	}
	/**
	 * 转换本地文件为数组(小文件转换使用，大文件千万别用)
	 * 
	 * @author shengli
	 * @date 2015年4月17日
	 * @param File
	 * @return
	 */
	public static byte[] file2ByteArray(File file){
		byte[] ret = null; 
		InputStream in=null;
		ByteArrayOutputStream  out =null;
		try {
			in = new FileInputStream(file);
			out=new ByteArrayOutputStream();
			byte[] b =new byte[1024];
			int n =-1;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			ret=out.toByteArray();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public static File byteArray2File(byte[] fileByte,String filePath,String fileName){
		File file =null;
		if(new File(filePath).exists()){
			file =new File(filePath+fileName);
			OutputStream out=null;
			try {
				out =new FileOutputStream(file);
				out.write(fileByte);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return file;
	}

	/**
	 * 获取文件扩展名
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName) {
		if (StringUtil.isEmpty(fileName))
			return "";

		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}

    public static File saveInputStreamToTempFile(InputStream in, String name) throws IOException {
        File file = TemporaryAttachmentStore.getFileForWriting(MailChat.app, name);
        OutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);
        out.flush();
        out.close();
        in.close();
        return file;
    }
}
