//var nothin = "to see here";
//function changeHeading(){
//document.getElementById("heading").innerHTML = "Bhashithas Web Page";
//}

// https://raw.githubusercontent.com/js-cookie/js-cookie/master/src/js.cookie.js

$(document).ready(function() {
    $("#heading").html("Welcome To City Search");
    
    var valueArray = [];  
    var results = []; 
    
    // seting the textField value accoriding to the cookies.
    $("#tags").val(Cookies.get("query"));
    $("#tags").focus();
    
     $("#tags").keyup(function(){
            var query = $("#tags").val();
         
            // setting the cookie
            Cookies.set("query", query);
         
            var host = window.location.host;
            // var port = window.location.port;
                                    
            var url = "http://" + host + "/?q=" + query;
            // var url = "http://localhost:2222" +
            // "/?q=" + query;
         console.log("URL: "+ url);
         $.get(url, function(result){
             console.log(result);
                                          
             // $("#result").html("<p>Query: " +
             // query + "</p>"
             // + "<p>Result: " +
             // result["results"].join("; ") +
             // "</p>");
             // $("#result").html("");
             results = result
             valueArray = [];
             // var text = $("#result").val()
             for(const item in result["results"]){
                                          
                 // text += "<br/>" +
                 // result["results"][item]["city"];
                 // $("#result").html(text);
                 valueArray.push(result["results"][item].city);
             }
//             console.log(valueArray);
            
             $( "#tags" ).autocomplete({
                minLength: 0,
//                 disabled: true,
                 source: valueArray
                 ,focus: function( event, ui ) {
                  $( "#tags" ).val( ui.item.value );
//                     console.log(ui.item);
                     return true;
                }
                , select: function( event, ui ) {
                     $( "#tags" ).val( ui.item.value );
                    console.log(ui.item);
                    console.log(valueArray.indexOf(ui.item.value));
                     return false;
                 }
//                 ,$( "#tags" ).autocomplete( "search", "" )
             })
              $( "#tags" ).autocomplete( "search", "" );
             
//                 .data( "ui-autocomplete" )._renderItem = function( ul, item ) {
//                console.log(item.city);
//                 return $( "<li>" )
//               .append( "<a>" + item.city.value + "</a>" )
//               .appendTo( ul );
//            };
         })
     })
     
     
     $("#btnSubmit").click(function(){
        console.log("clickedddd");
        console.log(valueArray.indexOf($( "#tags" ).val()));
         var object = results["results"][valueArray.indexOf($( "#tags" ).val())];
         
         console.log(object);
         var googleMapUrl = "https://www.google.com/maps/preview/@"+ object.lat+","+object.lon+",14.9z";
         
        var win = window.open(googleMapUrl, '_blank');
        win.focus();
    }); 

})