package de.deutschebahn.bahnhoflive.backend.local.model

object ComplaintableStation {
    val ids = setOf(
        "22",
        "23",
        "28",
//        "80",
        "85",
        "87",
        "108",
        "116",
        "7719",
        "169",
        "177",
        "187",
        "192",
        "202",
        "7966",
        "203",
        "207",
        "220",
        "237",
        "251",
        "264",
        "2886",
        "315",
        "316",
        "332",
        "6129",
        "334",
        "392",
        "393",
        "430",
        "4361",
        "450",
        "475",
        "503",
        "504",
        "520",
        "525",
        "6340",
        "526",
        "2035",
        "1071",
        "530",
        "4809",
        "5016",
        "4859",
        "533",
        "534",
        "53",
        "535",
        "536",
        "537",
        "538",
        "527",
        "539",
        "528",
        "540",
        "541",
        "542",
        "543",
        "544",
        "595",
        "545",
        "546",
        "547",
        "548",
        "549",
        "7720",
        "550",
        "551",
        "552",
        "553",
        "554",
        "532",
        "555",
        "556",
        "557",
        "559",
        "561",
        "563",
        "7721",
        "565",
        "566",
        "6723",
        "567",
        "568",
        "571",
        "7910",
        "591",
        "592",
        "7726",
        "7958",
        "622",
        "623",
        "811",
        "8281",
        "6792",
        "628",
        "631",
        "639",
        "643",
        "652",
        "655",
        "660",
        "661",
        "688",
        "723",
        "724",
        "763",
        "4568",
        "767",
        "779",
        "780",
        "782",
        "783",
        "785",
        "791",
        "801",
        "803",
        "814",
        "816",
        "835",
        "840",
        "855",
        "8251",
        "888",
        "951",
        "963",
        "968",
        "970",
        "972",
        "1028",
        "1040",
        "1056",
        "1062",
        "8248",
        "1077",
        "1104",
        "1108",
        "1126",
        "1141",
        "1146",
        "1180",
        "1289",
        "1341",
        "1343",
        "1352",
        "1374",
        "1390",
        "1401",
        "1484",
        "1491",
        "7722",
        "1501",
        "1507",
        "1537",
        "1590",
        "1610",
        "1634",
        "1641",
        "1645",
        "1659",
        "1683",
        "1690",
        "1782",
        "1787",
        "1793",
        "8192",
        "1821",
        "1866",
        "7982",
        "1889",
        "1893",
        "1901",
        "1932",
        "1944",
        "1967",
        "1969",
        "1973",
        "2008",
        "2109",
        "2120",
        "2218",
        "529",
        "2262",
        "2268",
        "2288",
        "2391",
        "2438",
        "2447",
        "2498",
        "2500",
        "7772",
        "2513",
        "2514",
        "2517",
        "2519",
        "2528",
        "733",
        "2621",
        "2545",
        "2610",
        "7728",
        "2622",
        "2623",
        "2628",
        "2632",
        "7729",
        "2678",
        "2681",
        "2689",
        "2691",
        "2708",
        "2716",
        "2743",
        "2747",
        "5817",
        "2760",
        "2767",
        "2790",
        "2832",
        "2866",
        "2884",
        "2890",
        "2900",
        "2901",
        "2912",
        "2923",
        "2924",
        "2927",
        "2928",
        "2930",
        "2162",
        "3821",
        "4820",
        "2944",
        "2961",
        "1670",
        "3493",
        "2998",
        "3006",
        "3008",
        "3012",
        "3032",
        "7759",
        "3067",
        "3094",
        "3095",
        "3096",
        "7723",
        "3107",
        "3127",
        "3135",
        "3200",
        "6660",
        "3201",
        "3299",
        "3318",
        "3320",
        "3329",
        "3343",
        "3394",
        "3402",
        "3420",
        "1496",
        "3750",
        "3463",
        "3464",
        "3487",
        "3491",
        "3511",
        "7144",
        "3611",
        "3617",
        "3631",
        "3658",
        "3662",
        "104",
        "2264",
        "4024",
        "3668",
        "3670",
        "3671",
        "3673",
        "3703",
        "7730",
        "3746",
        "3749",
        "3768",
        "3801",
        "3828",
        "915",
        "3832",
        "3847",
        "3856",
        "5032",
        "3857",
        "3871",
        "3872",
        "3881",
        "3891",
        "3898",
        "3925",
        "3942",
        "3947",
        "3987",
        "3997",
        "4027",
        "4032",
        "4053",
        "4054",
        "4066",
        "4076",
        "6840",
        "7727",
        "4079",
        "4081",
        "4092",
        "4120",
        "4204",
        "4234",
        "4241",
        "4266",
        "4280",
        "7655",
        "39",
        "135",
        "4546",
        "2771",
        "7813",
        "8247",
        "5928",
        "4329",
        "7908",
        "4382",
        "4385",
        "4425",
        "4492",
        "4522",
        "4557",
        "4566",
        "4582",
        "167",
        "4593",
        "4692",
        "4722",
        "4731",
        "4735",
        "4739",
        "7774",
        "4767",
        "4768",
        "7731",
        "4777",
        "4778",
        "7762",
        "890",
        "4846",
        "4847",
        "4848",
        "4880",
        "5824",
        "4854",
        "8356",
        "4905",
        "7662",
        "7732",
        "4950",
        "4965",
        "4976",
        "4998",
        "5012",
        "5026",
        "5036",
        "5070",
        "4914",
        "5099",
        "5100",
        "5122",
        "5129",
        "5145",
        "5159",
        "5169",
        "5213",
        "5247",
        "5251",
        "4080",
        "5287",
        "5340",
        "2879",
        "5365",
        "5484",
        "5496",
        "5507",
        "5523",
        "5537",
        "5545",
        "5559",
        "5563",
        "5564",
        "5598",
        "558",
        "5659",
        "5665",
        "560",
        "5684",
        "7734",
        "5755",
        "5763",
        "5818",
        "5819",
        "800",
        "5825",
        "5839",
        "5842",
        "2957",
        "5844",
        "5854",
        "5876",
        "5896",
        "7736",
        "5934",
        "781",
        "997",
        "3369",
        "5996",
        "3030",
        "5999",
        "6028",
        "6042",
        "6058",
        "6059",
        "6060",
        "7761",
        "6066",
        "6071",
        "7146",
        "6123",
        "6164",
        "2871",
        "6217",
        "6251",
        "6298",
        "6323",
        "6335",
        "6337",
        "6336",
        "6428",
        "6447",
        "6453",
        "6454",
        "6466",
        "6472",
        "6537",
        "6539",
        "8249",
        "6550",
        "6551",
        "8214",
        "7756",
        "6617",
        "6664",
        "6683",
        "6686",
        "6689",
        "6706",
        "6707",
        "6708",
        "7760",
        "6720",
        "6724",
        "6731",
        "6744",
        "6763",
        "6771",
        "7590",
        "5415",
        "6807",
        "6824",
        "6871",
        "6898",
        "6899",
        "6939",
        "6940",
        "6945",
        "6967",
        "7755",
        "6998",
        "7010",
    )
}