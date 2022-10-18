// Inlined(
//   None,
//   Nil,
//   Inlined(
//     Some(Ident("fragment")),
//     Nil,
//     Typed(
//       Apply(
//         TypeApply(Select(Ident("ShaderContext"), "apply"), List(Inferred(), Inferred())),
//         List(
//           Block(
//             Nil,
//             Block(
//               List(
//                 DefDef(
//                   "$anonfun",
//                   List(TermParamClause(List(ValDef("env", Inferred(), None)))),
//                   Inferred(),
//                   Some(
//                     Block(
//                       List(
//                         ValDef("zero", Inferred(), Some(Literal(FloatConstant(0.0f)))),
//                         ValDef("alpha", Inferred(), Some(Literal(FloatConstant(1.0f))))
//                       ),
//                       TypeApply(
//                         Select(
//                           Inlined(
//                             Some(
//                               Apply(
//                                 TypeApply(Select(Ident("Program"), "apply"), List(Inferred())),
//                                 List(
//                                   Inlined(
//                                     Some(
//                                       Apply(
//                                         Select(Ident("rgba"), "apply"),
//                                         List(Select(Ident("env"), "UV"), Ident("zero"), Ident("alpha"))
//                                       )
//                                     ),
//                                     List(
//                                       ValDef("rgba$_this", Inferred(), Some(Ident("rgba"))),
//                                       ValDef("ShaderDSLTypes_this", Inferred(), Some(Ident("ShaderDSL")))
//                                     ),
//                                     Typed(
//                                       Apply(
//                                         Select(Inlined(None, Nil, Ident("rgba$_this")), "apply"),
//                                         List(
//                                           Select(Inlined(None, Nil, Select(Ident("env"), "UV")), "x"),
//                                           Select(Inlined(None, Nil, Select(Ident("env"), "UV")), "y"),
//                                           Inlined(None, Nil, Ident("zero")),
//                                           Inlined(None, Nil, Ident("alpha"))
//                                         )
//                                       ),
//                                       TypeIdent("rgba")
//                                     )
//                                   )
//                                 )
//                               )
//                             ),
//                             List(
//                               ValDef(
//                                 "$proxy2",
//                                 Inferred(),
//                                 Some(TypeApply(Select(Ident("Program$package"), "$asInstanceOf$"), List(Inferred())))
//                               ),
//                               ValDef("Program$package$_this", Inferred(), Some(Ident("Program$package"))),
//                               ValDef(
//                                 "value$proxy2",
//                                 Inferred(),
//                                 Some(
//                                   Inlined(
//                                     Some(
//                                       Apply(
//                                         Select(Ident("rgba"), "apply"),
//                                         List(Select(Ident("env"), "UV"), Ident("zero"), Ident("alpha"))
//                                       )
//                                     ),
//                                     List(
//                                       ValDef("rgba$_this", Inferred(), Some(Ident("rgba"))),
//                                       ValDef("ShaderDSLTypes_this", Inferred(), Some(Ident("ShaderDSL")))
//                                     ),
//                                     Typed(
//                                       Apply(
//                                         Select(Inlined(None, Nil, Ident("rgba$_this")), "apply"),
//                                         List(
//                                           Select(Inlined(None, Nil, Select(Ident("env"), "UV")), "x"),
//                                           Select(Inlined(None, Nil, Select(Ident("env"), "UV")), "y"),
//                                           Inlined(None, Nil, Ident("zero")),
//                                           Inlined(None, Nil, Ident("alpha"))
//                                         )
//                                       ),
//                                       TypeIdent("rgba")
//                                     )
//                                   )
//                                 )
//                               )
//                             ),
//                             Typed(
//                               Inlined(None, Nil, Ident("value$proxy2")),
//                               Applied(TypeIdent("Program"), List(Inferred()))
//                             )
//                           ),
//                           "$asInstanceOf$"
//                         ),
//                         List(Inferred())
//                       )
//                     )
//                   )
//                 )
//               ),
//               Closure(Ident("$anonfun"), None)
//             )
//           )
//         )
//       ),
//       Applied(TypeIdent("ShaderContext"), List(TypeIdent("FragEnv"), TypeIdent("rgba")))
//     )
//   )
// )
