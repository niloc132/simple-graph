function simplegraph(){var P='',xb='" for "gwt:onLoadErrorFn"',vb='" for "gwt:onPropertyErrorFn"',ib='"><\/script>',Z='#',Kc='.cache.html',_='/',lb='//',kc='0CD17C974575C5A6F1D0305BBA62D794',oc='11DE360FA005BF2B2AD7E32E940172B2',tc='605E41A39B356F60765D64848A468032',Gc='781CB54E5245ABF2390AEC601D27CF19',Hc='888065A711FCD6521D1F4E36BBB2EFB3',Jc=':',lc=':1',uc=':10',vc=':11',wc=':12',xc=':13',yc=':14',zc=':15',Ac=':16',Bc=':17',Cc=':18',Dc=':19',mc=':2',nc=':3',pc=':4',qc=':5',rc=':6',sc=':7',Ec=':8',Fc=':9',pb='::',Sc='<script defer="defer">simplegraph.onInjectionDone(\'simplegraph\')<\/script>',hb='<script id="',sb='=',$='?',Ic='AE9D71543D2961D8A92F9C615546104A',ub='Bad handler "',Rc='DOMContentLoaded',jb='SCRIPT',gb='__gwt_marker_simplegraph',Xb='adobeair',Yb='air',kb='base',cb='baseUrl',T='begin',S='bootstrap',Db='chrome',bb='clear.cache.gif',rb='content',Lc='css/chart.css',Y='end',Tb='gecko',Vb='gecko1_8',Wb='gecko1_9',U='gwt.codesvr=',V='gwt.hosted=',W='gwt.hybrid',wb='gwt:onLoadErrorFn',tb='gwt:onPropertyErrorFn',qb='gwt:property',Cb='gxt.user.agent',Qc='head',ic='hosted.html?simplegraph',Pc='href',Gb='ie10',Mb='ie6',Kb='ie7',Ib='ie8',Hb='ie9',yb='iframe',ab='img',zb="javascript:''",Mc='link',ec='linux',hc='loadExternalRefs',dc='mac',cc='mac os x',bc='macintosh',mb='meta',Bb='moduleRequested',X='moduleStartup',Fb='msie',Lb='msie 6',Jb='msie 7',nb='name',Eb='opera',Ab='position:absolute;width:0;height:0;border:none',Nc='rel',Ub='rv:1.8',Nb='safari',Pb='safari3',Rb='safari4',Sb='safari5',db='script',jc='selectingPermutation',Q='simplegraph',eb='simplegraph.nocache.js',ob='simplegraph::',R='startup',Oc='stylesheet',fb='undefined',_b='unknown',Zb='user.agent',ac='user.agent.os',Ob='version/3',Qb='version/4',$b='webkit',gc='win32',fc='windows';var m=window,n=document,o=m.__gwtStatsEvent?function(a){return m.__gwtStatsEvent(a)}:null,p=m.__gwtStatsSessionId?m.__gwtStatsSessionId:null,q,r,s,t=P,u={},v=[],w=[],x=[],y=0,z,A;o&&o({moduleName:Q,sessionId:p,subSystem:R,evtGroup:S,millis:(new Date).getTime(),type:T});if(!m.__gwt_stylesLoaded){m.__gwt_stylesLoaded={}}if(!m.__gwt_scriptsLoaded){m.__gwt_scriptsLoaded={}}function B(){var b=false;try{var c=m.location.search;return (c.indexOf(U)!=-1||(c.indexOf(V)!=-1||m.external&&m.external.gwtOnLoad))&&c.indexOf(W)==-1}catch(a){}B=function(){return b};return b}
function C(){if(q&&r){var b=n.getElementById(Q);var c=b.contentWindow;if(B()){c.__gwt_getProperty=function(a){return H(a)}}simplegraph=null;c.gwtOnLoad(z,Q,t,y);o&&o({moduleName:Q,sessionId:p,subSystem:R,evtGroup:X,millis:(new Date).getTime(),type:Y})}}
function D(){function e(a){var b=a.lastIndexOf(Z);if(b==-1){b=a.length}var c=a.indexOf($);if(c==-1){c=a.length}var d=a.lastIndexOf(_,Math.min(c,b));return d>=0?a.substring(0,d+1):P}
function f(a){if(a.match(/^\w+:\/\//)){}else{var b=n.createElement(ab);b.src=a+bb;a=e(b.src)}return a}
function g(){var a=F(cb);if(a!=null){return a}return P}
function h(){var a=n.getElementsByTagName(db);for(var b=0;b<a.length;++b){if(a[b].src.indexOf(eb)!=-1){return e(a[b].src)}}return P}
function i(){var a;if(typeof isBodyLoaded==fb||!isBodyLoaded()){var b=gb;var c;n.write(hb+b+ib);c=n.getElementById(b);a=c&&c.previousSibling;while(a&&a.tagName!=jb){a=a.previousSibling}if(c){c.parentNode.removeChild(c)}if(a&&a.src){return e(a.src)}}return P}
function j(){var a=n.getElementsByTagName(kb);if(a.length>0){return a[a.length-1].href}return P}
function k(){var a=n.location;return a.href==a.protocol+lb+a.host+a.pathname+a.search+a.hash}
var l=g();if(l==P){l=h()}if(l==P){l=i()}if(l==P){l=j()}if(l==P&&k()){l=e(n.location.href)}l=f(l);t=l;return l}
function E(){var b=document.getElementsByTagName(mb);for(var c=0,d=b.length;c<d;++c){var e=b[c],f=e.getAttribute(nb),g;if(f){f=f.replace(ob,P);if(f.indexOf(pb)>=0){continue}if(f==qb){g=e.getAttribute(rb);if(g){var h,i=g.indexOf(sb);if(i>=0){f=g.substring(0,i);h=g.substring(i+1)}else{f=g;h=P}u[f]=h}}else if(f==tb){g=e.getAttribute(rb);if(g){try{A=eval(g)}catch(a){alert(ub+g+vb)}}}else if(f==wb){g=e.getAttribute(rb);if(g){try{z=eval(g)}catch(a){alert(ub+g+xb)}}}}}}
function F(a){var b=u[a];return b==null?null:b}
function G(a,b){var c=x;for(var d=0,e=a.length-1;d<e;++d){c=c[a[d]]||(c[a[d]]=[])}c[a[e]]=b}
function H(a){var b=w[a](),c=v[a];if(b in c){return b}var d=[];for(var e in c){d[c[e]]=e}if(A){A(a,d,b)}throw null}
var I;function J(){if(!I){I=true;var a=n.createElement(yb);a.src=zb;a.id=Q;a.style.cssText=Ab;a.tabIndex=-1;n.body.appendChild(a);o&&o({moduleName:Q,sessionId:p,subSystem:R,evtGroup:X,millis:(new Date).getTime(),type:Bb});a.contentWindow.location.replace(t+L)}}
w[Cb]=function(){var a=navigator.userAgent.toLowerCase();if(a.indexOf(Db)!=-1)return Db;if(a.indexOf(Eb)!=-1)return Eb;if(a.indexOf(Fb)!=-1){if(n.documentMode>=10)return Gb;if(n.documentMode>=9)return Hb;if(n.documentMode>=8)return Ib;if(a.indexOf(Jb)!=-1)return Kb;if(a.indexOf(Lb)!=-1)return Mb;return Gb}if(a.indexOf(Nb)!=-1){if(a.indexOf(Ob)!=-1)return Pb;if(a.indexOf(Qb)!=-1)return Rb;return Sb}if(a.indexOf(Tb)!=-1){if(a.indexOf(Ub)!=-1)return Vb;return Wb}if(a.indexOf(Xb)!=-1)return Yb;return null};v[Cb]={air:0,chrome:1,gecko1_8:2,gecko1_9:3,ie10:4,ie6:5,ie7:6,ie8:7,ie9:8,opera:9,safari3:10,safari4:11,safari5:12};w[Zb]=function(){var b=navigator.userAgent.toLowerCase();var c=function(a){return parseInt(a[1])*1000+parseInt(a[2])};if(function(){return b.indexOf(Eb)!=-1}())return Eb;if(function(){return b.indexOf($b)!=-1}())return Nb;if(function(){return b.indexOf(Fb)!=-1&&n.documentMode>=9}())return Hb;if(function(){return b.indexOf(Fb)!=-1&&n.documentMode>=8}())return Ib;if(function(){var a=/msie ([0-9]+)\.([0-9]+)/.exec(b);if(a&&a.length==3)return c(a)>=6000}())return Mb;if(function(){return b.indexOf(Tb)!=-1}())return Vb;return _b};v[Zb]={gecko1_8:0,ie6:1,ie8:2,ie9:3,opera:4,safari:5};w[ac]=function(){var a=m.navigator.userAgent.toLowerCase();if(a.indexOf(bc)!=-1||a.indexOf(cc)!=-1){return dc}if(a.indexOf(ec)!=-1){return ec}if(a.indexOf(fc)!=-1||a.indexOf(gc)!=-1){return fc}return _b};v[ac]={linux:0,mac:1,unknown:2,windows:3};simplegraph.onScriptLoad=function(){if(I){r=true;C()}};simplegraph.onInjectionDone=function(){q=true;o&&o({moduleName:Q,sessionId:p,subSystem:R,evtGroup:hc,millis:(new Date).getTime(),type:Y});C()};E();D();var K;var L;if(B()){if(m.external&&(m.external.initModule&&m.external.initModule(Q))){m.location.reload();return}L=ic;K=P}o&&o({moduleName:Q,sessionId:p,subSystem:R,evtGroup:S,millis:(new Date).getTime(),type:jc});if(!B()){try{G([Ib,Ib,ec],kc);G([Ib,Ib,dc],kc+lc);G([Ib,Ib,_b],kc+mc);G([Ib,Ib,fc],kc+nc);G([Gb,Hb,ec],oc);G([Gb,Hb,dc],oc+lc);G([Gb,Hb,_b],oc+mc);G([Gb,Hb,fc],oc+nc);G([Hb,Hb,ec],oc+pc);G([Hb,Hb,dc],oc+qc);G([Hb,Hb,_b],oc+rc);G([Hb,Hb,fc],oc+sc);G([Yb,Nb,ec],tc);G([Yb,Nb,dc],tc+lc);G([Pb,Nb,_b],tc+uc);G([Pb,Nb,fc],tc+vc);G([Rb,Nb,ec],tc+wc);G([Rb,Nb,dc],tc+xc);G([Rb,Nb,_b],tc+yc);G([Rb,Nb,fc],tc+zc);G([Sb,Nb,ec],tc+Ac);G([Sb,Nb,dc],tc+Bc);G([Sb,Nb,_b],tc+Cc);G([Sb,Nb,fc],tc+Dc);G([Yb,Nb,_b],tc+mc);G([Yb,Nb,fc],tc+nc);G([Db,Nb,ec],tc+pc);G([Db,Nb,dc],tc+qc);G([Db,Nb,_b],tc+rc);G([Db,Nb,fc],tc+sc);G([Pb,Nb,ec],tc+Ec);G([Pb,Nb,dc],tc+Fc);G([Mb,Mb,ec],Gc);G([Mb,Mb,dc],Gc+lc);G([Mb,Mb,_b],Gc+mc);G([Mb,Mb,fc],Gc+nc);G([Kb,Mb,ec],Gc+pc);G([Kb,Mb,dc],Gc+qc);G([Kb,Mb,_b],Gc+rc);G([Kb,Mb,fc],Gc+sc);G([Vb,Vb,ec],Hc);G([Vb,Vb,dc],Hc+lc);G([Vb,Vb,_b],Hc+mc);G([Vb,Vb,fc],Hc+nc);G([Wb,Vb,ec],Hc+pc);G([Wb,Vb,dc],Hc+qc);G([Wb,Vb,_b],Hc+rc);G([Wb,Vb,fc],Hc+sc);G([Eb,Eb,ec],Ic);G([Eb,Eb,dc],Ic+lc);G([Eb,Eb,_b],Ic+mc);G([Eb,Eb,fc],Ic+nc);K=x[H(Cb)][H(Zb)][H(ac)];var M=K.indexOf(Jc);if(M!=-1){y=Number(K.substring(M+1));K=K.substring(0,M)}L=K+Kc}catch(a){return}}var N;function O(){if(!s){s=true;if(!__gwt_stylesLoaded[Lc]){var a=n.createElement(Mc);__gwt_stylesLoaded[Lc]=a;a.setAttribute(Nc,Oc);a.setAttribute(Pc,t+Lc);n.getElementsByTagName(Qc)[0].appendChild(a)}C();if(n.removeEventListener){n.removeEventListener(Rc,O,false)}if(N){clearInterval(N)}}}
if(n.addEventListener){n.addEventListener(Rc,function(){J();O()},false)}var N=setInterval(function(){if(/loaded|complete/.test(n.readyState)){J();O()}},50);o&&o({moduleName:Q,sessionId:p,subSystem:R,evtGroup:S,millis:(new Date).getTime(),type:Y});o&&o({moduleName:Q,sessionId:p,subSystem:R,evtGroup:hc,millis:(new Date).getTime(),type:T});n.write(Sc)}
simplegraph();